package com.example.refile.service;

import com.example.refile.util.HttpUtil;
import com.example.refile.model.Attachment;
import com.example.refile.model.User;
import com.example.refile.repository.UserRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GmailService {

    private static final String APPLICATION_NAME = "ReFile";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private NetHttpTransport httpTransport = null;
    private final AuthService authService;
    private final GCSService gcsService;
    private final UserRepository userRepository;

    public GmailService(AuthService authService, GCSService gcsService, UserRepository userRepository) {
        try {
            this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        this.authService = authService;
        this.gcsService = gcsService;
        this.userRepository = userRepository;
    }

    public List<Attachment> getAttachments(Long userId) {
        User user = userRepository.findById(userId).get();
        return user.getAttachments();
    }

    public void writeAttachments(Long userId) {
        try {
            Gmail service = getGmailClient(String.valueOf(userId));

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

    private Gmail getGmailClient(String userId) {
        Credential credential = authService.getCredentials(userId);
        // need to create a new Gmail instance for every invocation of this method call

        return new Gmail.Builder(HttpUtil.getHttpTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
