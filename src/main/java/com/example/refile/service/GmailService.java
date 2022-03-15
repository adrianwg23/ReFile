package com.example.refile.service;

import com.example.refile.model.Attachment;
import com.example.refile.model.User;
import com.example.refile.util.HttpUtil;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.refile.util.Constants.APPLICATION_NAME;

@Service
@RequiredArgsConstructor
public class GmailService {

    // default user id for authenticated users querying gmail
    private static final String USER_ID = "me";

    private static final Logger logger = LoggerFactory.getLogger(GmailService.class);

    private final CategorizationService categorizationService;
    private final CredentialService credentialService;
    private final AttachmentService attachmentService;

    @Qualifier("ioExecutor")
    private final ExecutorService ioExecutor;

    @Qualifier("cpuExecutor")
    private final ExecutorService cpuExecutor;

    /**
     * Retrieves attachments for a given user from the database. If attachments are empty, a sync will be be made to
     * fetch attachments from Gmail.
     *
     * @param user User
     * @return List of Attachments belonging to the user
     */
    public List<Attachment> getAttachments(User user) throws IOException {
        List<Attachment> attachments = attachmentService.getAttachmentsByUser(user);

        if (attachments.isEmpty()) {
            return syncAttachments(user);
        }
        return attachments;
    }

    public List<Attachment> syncAttachments(User user) throws IOException {
        long startTime = System.currentTimeMillis();
        attachmentService.deleteAllAttachments(user.getAttachments());
        user.getAttachments().clear();

        Gmail gmail = getGmailClient(user.getUserId());

        logger.info("getting message ids with attachments");
        List<Message> messageIds = getMessageIdsWithAttachments(gmail);
        Collections.reverse(messageIds);
        Set<String> seenThreads = Sets.newConcurrentHashSet();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        logger.info("processing messages");
        for (Message message : messageIds) {
            futures.add(CompletableFuture.supplyAsync(() -> getFullMessage(message, gmail), ioExecutor)
                                         .thenApplyAsync(fullMessage -> processMessage(fullMessage, user, seenThreads), cpuExecutor)
                                         .thenAccept(a -> a.ifPresent(attachmentService::saveAllAttachments)));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long endTime = System.currentTimeMillis();
        logger.info("That took " + (endTime - startTime) / 1000.0 + " seconds");
        return attachmentService.getAttachmentsByUser(user);
    }

    private Optional<List<Attachment>> processMessage(Message message, User user, Set<String> seenThreads) {
        String threadId = extractThreadId(message);
        if (seenThreads.contains(threadId)) {
            return Optional.empty();
        } else {
            seenThreads.add(threadId);
        }

        List<Attachment> attachments = new ArrayList<>();
        Set<String> seenCategories = new HashSet<>();

        List<MessagePart> parts = message.getPayload().getParts();
        if (parts == null) {
            return Optional.empty();
        }

        // header data
        String sender = null;
        String senderEmail = null;
        String subject = null;
        String thread = null;
        String cc;
        Set<String> ccSet = new HashSet<>();
        String receiver;
        Set<String> receiverSet = new HashSet<>();
        String importance = null;
        boolean headerProcessed = false;
        List<String> threadCategoryExtraction = new ArrayList<>();

        // email body data
        String body = null;
        List<String> bodyCategoryExtraction = new ArrayList<>();

        // attachment data
        for (int i = 1; i < parts.size(); i++) {
            MessagePart attachmentPart = parts.get(i);
            String fileName = attachmentPart.getFilename();
            if (fileName.isEmpty()) {
                return Optional.empty();
            }
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (extension.equals("ics")) {
                return Optional.empty();
            }
            if (!headerProcessed) {
                String[] headers = extractMessageHeaders(message);
                sender = headers[0];
                senderEmail = extractEmail(sender);
                subject = headers[1];
                thread = headers[2];
                cc = headers[3];
                ccSet = extractEmails(cc);
                receiver = headers[4];
                receiverSet = extractEmails(receiver);
                importance = headers[5];
                threadCategoryExtraction.addAll(categorizationService.extractCategories(thread, user.getCategories(),
                        seenCategories));
                seenCategories.addAll(threadCategoryExtraction);
                headerProcessed = true;
            }
            if (body == null) {
                body = extractEmailBody(parts.get(0));
                if (!body.isEmpty()) {
                    bodyCategoryExtraction.addAll(categorizationService.extractCategories(body, user.getCategories(),
                            seenCategories));
                    seenCategories.addAll(bodyCategoryExtraction);
                }
            }

            String attachmentId = attachmentPart.getBody().getAttachmentId();

            Attachment attachment = Attachment.builder()
                                              .user(user)
                                              .createdDate(Date.from(Instant.ofEpochMilli(message.getInternalDate())))
                                              .name(fileName)
                                              .sender(sender)
                                              .senderEmail(senderEmail)
                                              .receiver(receiverSet)
                                              .thread(thread)
                                              .subject(subject)
                                              .cc(ccSet)
                                              .body(body)
                                              .importance(importance)
                                              .extension(extension)
                                              .gId(attachmentId)
                                              .labelIds(new HashSet<>(message.getLabelIds()))
                                              .categories(new HashSet<>())
                                              .build();

            List<String> fileNameCategoryExtraction = new ArrayList<>();
            if (!fileName.isEmpty()) {
                fileNameCategoryExtraction.addAll(categorizationService.extractCategories(fileName, user.getCategories(),
                        seenCategories));
            }
            attachment.getCategories().addAll(threadCategoryExtraction);
            attachment.getCategories().addAll(bodyCategoryExtraction);
            attachment.getCategories().addAll(fileNameCategoryExtraction);

//            MessagePartBody attachmentPartBody = getAttachmentPartBody(message.getId(), attachmentId, gmail);

            attachments.add(attachment);
        }

        return Optional.of(attachments);
    }

    private String extractThreadId(Message message) {
        return message.getThreadId();
    }

    private Set<String> extractEmails(String emails) {
        if (emails == null) {
            return new HashSet<>();
        }
        Set<String> emailSet = new HashSet<>();
        String[] tmp = emails.split(",");
        for (String e : tmp) {
            String email = extractEmail(e);
            if (!email.isEmpty() && email.contains("@")) {
                emailSet.add(email);
            }
        }
        if (tmp.length == 0) {
            String email = extractEmail(emails);
            emailSet.add(email);
        }

        return emailSet;
    }

    private String extractEmail(String email) {
        String e = email.trim();
        Pattern pattern = Pattern.compile("<(.*?)>");
        Matcher matcher = pattern.matcher(e);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return e;
    }

    private String[] extractMessageHeaders(Message message) {
        String[] headers = new String[6];

        message.getPayload().getHeaders().forEach(header -> {
            String name = header.getName();
            if ("From".equals(name)) {
                headers[0] = header.getValue();
            } else if ("Subject".equals(name)) {
                headers[1] = header.getValue();
            } else if ("Thread-Topic".equals(name)) {
                headers[2] = header.getValue();
            } else if ("CC".equals(name)) {
                headers[3] = header.getValue();
            } else if ("To".equals(name)) {
                headers[4] = header.getValue();
            } else if ("Importance".equals(name)) {
                headers[5] = header.getValue();
            }
        });

        if (headers[2] == null) {
            headers[2] = headers[1];
        }

        return headers;
    }

    private String extractEmailBody(MessagePart messagePart) {
        MessagePart currPart = messagePart;
        if (currPart.getParts() != null) {
            while (currPart.getBody().getData() == null) {
                currPart = currPart.getParts().get(0);
            }
        }

        if (currPart.getBody().getData() != null) {
            return new String(Base64.decodeBase64(currPart.getBody().getData()),
                    StandardCharsets.UTF_8);
        }
        return "";
    }

    private MessagePartBody getAttachmentPartBody(String messageId, String attachmentId, Gmail gmail) throws IOException {
        return gmail.users()
                    .messages()
                    .attachments()
                    .get(USER_ID, messageId, attachmentId).execute();
    }

    private List<Message> getMessageIdsWithAttachments(Gmail gmail) throws IOException {
        Gmail.Users.Messages.List request = gmail.users().messages().list(USER_ID).setQ("has:attachment");

        ListMessagesResponse response = request.execute();
        List<Message> messageIdList = new ArrayList<>(response.getMessages());

        while (response.getNextPageToken() != null) {
            String nextPageToken = response.getNextPageToken();
            request = gmail.users().messages().list(USER_ID)
                           .setPageToken(nextPageToken)
                           .setQ("has:attachment");

            response = request.execute();
            messageIdList.addAll(response.getMessages());
        }

        return messageIdList;
    }

    private Message getFullMessage(Message message, Gmail gmail) {
        int count = 0;
        int maxTries = 3;
        while (true) {
            try {
                return gmail.users().messages().get(USER_ID, message.getId()).execute();
            } catch (Exception e) {
                if (++count == maxTries) throw new RuntimeException(e.getMessage());
            }
        }
    }

    /**
     * Returns a new Gmail Client instance
     *
     * @param userId User ID of the user
     * @return Gmail
     */
    private Gmail getGmailClient(Long userId) {
        Credential credential = credentialService.getCredential(userId).orElseThrow();
        return new Gmail.Builder(HttpUtil.getHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
