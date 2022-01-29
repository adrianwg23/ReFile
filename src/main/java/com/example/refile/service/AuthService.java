package com.example.refile.service;

import com.example.refile.model.User;
import com.example.refile.repository.UserRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.example.refile.config.AuthorizationConfiguration.CALLBACK_URL;

@Service
@AllArgsConstructor
public class AuthService {

    private final GoogleAuthorizationCodeFlow authorizationFlow;
    private final UserRepository userRepository;

    /**
     * Authenticates and authorize a user given an auth code. Returns the User ID of the user.
     *
     * @param authCode Auth code to exchange for Access and Refresh tokens from Google OAuth servers
     * @return User ID of authorized user
     */
    public Long authorize(String authCode) throws IOException {
        authCode = URLDecoder.decode(authCode, StandardCharsets.UTF_8);

        // Authorize the OAuth2 token.
        GoogleAuthorizationCodeTokenRequest tokenRequest = authorizationFlow.newTokenRequest(authCode);
        tokenRequest.setRedirectUri(CALLBACK_URL);
        GoogleTokenResponse tokenResponse = tokenRequest.execute();
        String email = tokenResponse.parseIdToken().getPayload().getEmail();

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;

        if (optionalUser.isEmpty()) {
            user = new User(email);
            userRepository.save(user);
            authorizationFlow.createAndStoreCredential(tokenResponse, String.valueOf(user.getUserId()));
        } else {
            user = optionalUser.get();
            Credential credential = authorizationFlow.loadCredential(String.valueOf(user.getUserId()));
            credential.refreshToken();
        }

        return user.getUserId();
    }

    public Credential getCredentials(String userId) {
        try {
            return authorizationFlow.loadCredential(userId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
