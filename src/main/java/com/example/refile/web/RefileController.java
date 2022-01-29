package com.example.refile.web;

import com.example.refile.model.Attachment;
import com.example.refile.service.AuthService;
import com.example.refile.service.GmailService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
public class RefileController {

    private final GmailService gmailService;
    private final AuthService authService;

    @GetMapping("/login")
    public ResponseEntity<Object> login(@RequestParam String authCode) {
        // TODO: return some sort of sessionID to be stored in a cookie (need to save this session in a db somewhere)
        try {
            Long userId = authService.authorize(authCode);
            return ResponseEntity.ok(userId);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal Server Error");
        }
    }

    // can remove once integrated with Frontend
    @GetMapping("/oauth2callback")
    public void oauth2callback() {

    }

    @GetMapping("/attachments")
    public ResponseEntity<List<Attachment>> attachments(@RequestParam Long userId) {
        // TODO: check if session id is valid first
        return ResponseEntity.ok(gmailService.getAttachments(userId));
    }

    @PostMapping("sync-attachments")
    public void syncAttachments(@RequestParam Long userId) {
        gmailService.writeAttachments(userId);
    }

//    public Credential getCredential() throws IOException {
//        String authorizeUrl =
//                authorizationFlow.newAuthorizationUrl().setRedirectUri(CALLBACK_URL).build();
//        System.out.printf("Paste this url in your browser:%n%s%n", authorizeUrl);
//
//        // Wait for the authorization code.
//        System.out.println("Type the code you received here: ");
//        @SuppressWarnings("DefaultCharset") // Reading from stdin, so default charset is appropriate.
//                String authorizationCode = new BufferedReader(new InputStreamReader(System.in)).readLine();
//
//        authorizationCode = URLDecoder.decode(authorizationCode, StandardCharsets.UTF_8);
//        System.out.println(authorizationCode);
//
//        // Authorize the OAuth2 token.
//        GoogleAuthorizationCodeTokenRequest tokenRequest =
//                authorizationFlow.newTokenRequest(authorizationCode);
//        tokenRequest.setRedirectUri(CALLBACK_URL);
//        GoogleTokenResponse tokenResponse = tokenRequest.execute();
////        String hashedUserId = hashString(tokenResponse.parseIdToken().getPayload().getEmail());
//
//        // Store the credential for the user.
//        return authorizationFlow.createAndStoreCredential(tokenResponse, "test");
//    }
}
