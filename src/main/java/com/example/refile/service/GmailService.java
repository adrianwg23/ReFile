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
import java.util.ArrayList;
import java.util.List;

import static com.example.refile.util.Constants.APPLICATION_NAME;

@Service
@RequiredArgsConstructor
public class GmailService {

    // default user id for authenticated users querying gmail
    private static final String USER_ID = "me";

    private final GCSService gcsService;
    private final CredentialService credentialService;

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
            syncAttachments(user);
        }
        return user.getAttachments();
    }

    public void syncAttachments(User user) throws IOException {
        Gmail gmail = getGmailClient(user.getUserId());
        List<Message> messageList = getMessagesWithAttachments(gmail);

        for (Message message : messageList) {
            System.out.println("\n--------------\n");
            System.out.println(message);
            for (MessagePart part : message.getPayload().getParts()) {
                if (part.getBody().getAttachmentId() != null) {
                    String fileName = part.getFilename();
                    String attachmentId = part.getBody().getAttachmentId();
                    MessagePartBody attachmentPart = gmail.users()
                                                            .messages()
                                                            .attachments()
                                                            .get(USER_ID, message.getId(), attachmentId).execute();

                    byte[] attachmentData = Base64.decodeBase64(attachmentPart.getData());
                    this.gcsService.write(GCSService.ATTACHMENTS_BUCKET, fileName, attachmentData);
                    // TODO: Create and persist attachment objects, figure out a way to get blob gcs link
                }
            }
        }
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
        Credential credential = credentialService.getCredential(userId);
        return new Gmail.Builder(HttpUtil.getHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
