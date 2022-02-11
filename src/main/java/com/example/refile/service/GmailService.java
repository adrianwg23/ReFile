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
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.example.refile.util.Constants.APPLICATION_NAME;

@Service
@RequiredArgsConstructor
public class GmailService {

    // default user id for authenticated users querying gmail
    private static final String USER_ID = "me";

    private final CategorizationService categorizationService;
    private final CredentialService credentialService;
    private final AttachmentService attachmentService;

    /**
     * Retrieves attachments for a given user from the database. If attachments are empty, a sync will be be made to
     * fetch attachments from Gmail.
     *
     * @param user User
     * @return List of Attachments belonging to the user
     */
    public List<Attachment> getAttachments(User user) throws IOException {
        List<Attachment> attachments = user.getAttachments();

        if (attachments.isEmpty()) {
            syncAttachments(user, false);
        }
        return user.getAttachments();
    }

    public List<Attachment> syncAttachments(User user, boolean refresh) throws IOException {
        if (refresh) {
            user.getAttachments().forEach(attachmentService::deleteAttachment);
            user.getAttachments().clear();
        }
        List<Attachment> attachments = new ArrayList<>();
        Gmail gmail = getGmailClient(user.getUserId());

        List<Message> messageList = getMessagesWithAttachments(gmail);

        // TODO: make each execute async
        for (Message message : messageList) {
            Attachment attachment = new Attachment();
            attachment.setUser(user);

            extractMessageMetadata(attachment, message);

            List<MessagePart> parts = message.getPayload().getParts();

            // email body
            extractEmailBody(attachment, parts.get(0));

            // attachment data
            MessagePart attachmentPart = parts.get(1);
            String fileName = attachmentPart.getFilename();
            String extension = fileName.substring(fileName.indexOf(".") + 1);
            attachment.setName(fileName);
            attachment.setExtension(extension);

            String attachmentId = attachmentPart.getBody().getAttachmentId();
//            MessagePartBody attachmentPartBody = getAttachmentPartBody(message.getId(), attachmentId, gmail);

            System.out.println(attachmentId);
            attachment.setGId(attachmentId);
            attachments.add(attachment);
            attachmentService.putAttachment(attachment);
        }

        user.setAttachments(attachments);

        return attachments;
    }

    private void extractMessageMetadata(Attachment attachment, Message message) {
        attachment.setLabelIds(new HashSet<>(message.getLabelIds()));
        attachment.setCreatedDate(Date.from(Instant.ofEpochMilli(message.getInternalDate())));

        message.getPayload().getHeaders().forEach(header -> {
            String name = header.getName();
            if ("From".equals(name)) {
                attachment.setSender(header.getValue());
            } else if ("Subject".equals(name)) {
                attachment.setSubject(header.getValue());
                List<String> categories = categorizationService.extractCategories(header.getValue());
                attachment.getCategories().addAll(categories);
            }
        });
    }

    private void extractEmailBody(Attachment attachment, MessagePart messagePart) {
        MessagePart currPart = messagePart;
        while (currPart.getBody().getData() == null) {
            currPart = currPart.getParts().get(0);
        }

        String emailBody = new String(Base64.decodeBase64(currPart.getBody().getData()),
                StandardCharsets.UTF_8);
        List<String> categories = categorizationService.extractCategories(emailBody);
        attachment.getCategories().addAll(categories);
    }

    private MessagePartBody getAttachmentPartBody(String messageId, String attachmentId, Gmail gmail) throws IOException {
        return gmail.users()
                    .messages()
                    .attachments()
                    .get(USER_ID, messageId, attachmentId).execute();
    }

    private List<Message> getMessagesWithAttachments(Gmail gmail) throws IOException {
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

        // TODO: make each execute async
        List<Message> messageList = new ArrayList<>();
        for (Message messageId : messageIdList) {
            Message message = gmail.users().messages().get(USER_ID, messageId.getId()).execute();
            messageList.add(message);
        }

        return messageList;
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
