package com.example.refile.web.auth;

import com.example.refile.service.GmailService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import com.google.auth.Credentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static com.example.refile.config.auth.AuthorizationConfiguration.CALLBACK_URL;

@RestController
public class AuthController {

    private final GoogleAuthorizationCodeFlow authorizationFlow;
    private final GmailService gmailService;
    private final Credentials credentials;

    public AuthController(GoogleAuthorizationCodeFlow authorizationFlow, GmailService gmailService,
                          Credentials credentials) {
        this.authorizationFlow = authorizationFlow;
        this.gmailService = gmailService;
        this.credentials = credentials;
    }


    @GetMapping("/")
    public String index() {
        return "hello world!";
    }

    @GetMapping("/attachments")
    public void attachments(@RequestParam String userId) throws IOException {
        gmailService.getAttachments(authorizationFlow.loadCredential(userId));
    }

    @GetMapping("/user")
    public Userinfo user() throws GeneralSecurityException, IOException {
        Credential credential = authorizationFlow.loadCredential("test");
        Oauth2 client = new Oauth2.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("ReFile")
                .build();

        Userinfo userinfo = client.userinfo().get().execute();
        return userinfo;
    }

    @GetMapping("/login")
    public String login() throws IOException {
        Credential credential = getCredential();

        Credential loadedCredential = authorizationFlow.loadCredential("test");
        System.out.println(credential.getAccessToken());
        System.out.println(loadedCredential.getAccessToken());
        return loadedCredential.toString();
    }

    @GetMapping("/oauth2callback")
    public void oauth2callback() {

    }

    public Credential getCredential() throws IOException {
        String authorizeUrl =
                authorizationFlow.newAuthorizationUrl().setRedirectUri(CALLBACK_URL).build();
        System.out.printf("Paste this url in your browser:%n%s%n", authorizeUrl);

        // Wait for the authorization code.
        System.out.println("Type the code you received here: ");
        @SuppressWarnings("DefaultCharset") // Reading from stdin, so default charset is appropriate.
                String authorizationCode = new BufferedReader(new InputStreamReader(System.in)).readLine();

        authorizationCode = URLDecoder.decode(authorizationCode, StandardCharsets.UTF_8);
        System.out.println(authorizationCode);

        // Authorize the OAuth2 token.
        GoogleAuthorizationCodeTokenRequest tokenRequest =
                authorizationFlow.newTokenRequest(authorizationCode);
        tokenRequest.setRedirectUri(CALLBACK_URL);
        GoogleTokenResponse tokenResponse = tokenRequest.execute();
//        String hashedUserId = hashString(tokenResponse.parseIdToken().getPayload().getEmail());

        // Store the credential for the user.
        return authorizationFlow.createAndStoreCredential(tokenResponse, "test");
    }

    private String hashString(String str) {
        byte[] digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytesOfMessage = str.getBytes(StandardCharsets.UTF_8);
            digest = md.digest(bytesOfMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert digest != null;
        return Base64.getEncoder().encodeToString(digest);
    }
}
