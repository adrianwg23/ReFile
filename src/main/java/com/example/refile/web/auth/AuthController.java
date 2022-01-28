package com.example.refile.web.auth;

import com.example.refile.service.GmailService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

import static com.example.refile.config.auth.AuthorizationConfiguration.CALLBACK_URL;

@RestController
public class AuthController {

    private final GoogleAuthorizationCodeFlow authorizationFlow;
    private final GmailService gmailService;

    public AuthController(GoogleAuthorizationCodeFlow authorizationFlow, GmailService gmailService) {
        this.authorizationFlow = authorizationFlow;
        this.gmailService = gmailService;
    }


    @GetMapping("/")
    public String index() {
        return "hello world!";
    }

    @GetMapping("/attachments")
    public void attachments() throws IOException {
        gmailService.getAttachments(authorizationFlow.loadCredential("test"));
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
    public Principal oauth2callback(Principal principal) {
        System.out.println(principal);
        return principal;
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

        // Store the credential for the user.
        return authorizationFlow.createAndStoreCredential(tokenResponse, "test");
    }
}
