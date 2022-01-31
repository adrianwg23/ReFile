package com.example.refile.service;

import com.example.refile.model.Attachment;
import com.example.refile.model.User;
import com.example.refile.repository.UserRepository;
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

import java.util.ArrayList;
import java.util.List;

import static com.example.refile.util.Constants.APPLICATION_NAME;

@Service
@RequiredArgsConstructor
public class GmailService {

    private final GCSService gcsService;
    private final CredentialService credentialService;

    /**
     * Retrieves attachments for a given user from the database. If attachments are empty, a sync will be be made to
     * fetch attachments from Gmail.
     *
     * @param user User
     * @return List of Attachments belonging to the user
     */
    public List<Attachment> getAttachments(User user) {
        List<Attachment> attachments = user.getAttachments();

        if (attachments.isEmpty()) {
            syncAttachments(user);
        }
        return user.getAttachments();
    }

    public void syncAttachments(User user) {
        try {
            Gmail service = getGmailClient(user.getUserId());

            Gmail.Users.Messages.List request = service.users().messages().list("me").setQ("has:attachment");

            ListMessagesResponse response = request.execute();
            List<Message> messageIdList = new ArrayList<>(response.getMessages());

            while (response.getNextPageToken() != null) {
                String nextPageToken = response.getNextPageToken();
                request = service.users().messages().list("me")
                                 .setPageToken(nextPageToken)
                                 .setQ("has:attachment");

                response = request.execute();
                messageIdList.addAll(response.getMessages());
            }

            List<Message> messageList = new ArrayList<>();
            for (Message messageId : messageIdList) {
                Message message = service.users().messages().get("me", messageId.getId()).execute();
                messageList.add(message);
            }

            for (Message message : messageList) {
                for (MessagePart part : message.getPayload().getParts()) {
                    if (part.getBody().getAttachmentId() != null) {
                        String fileName = part.getFilename();
                        String attachmentId = part.getBody().getAttachmentId();
                        MessagePartBody attachmentPart = service.users()
                                                                .messages()
                                                                .attachments()
                                                                .get("me", message.getId(), attachmentId).execute();

                        byte[] attachmentData = Base64.decodeBase64(attachmentPart.getData());
                        this.gcsService.write(GCSService.ATTACHMENTS_BUCKET, fileName, attachmentData);
                        // TODO: Create and persist attachment objects, figure out a way to get blob gcs link
                    }
                }
            }
        } catch(Exception ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * Returns a new Gmail Client instance
     *
     * @param userId User ID of the user
     * @return Gmail
     */
    private Gmail getGmailClient(Long userId) {
        Credential credential = credentialService.getCredential(userId);
        return new Gmail.Builder(HttpUtil.getHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
