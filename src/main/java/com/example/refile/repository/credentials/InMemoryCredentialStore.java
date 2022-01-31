package com.example.refile.repository.credentials;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.refile.util.Constants.REVOKE_TOKEN_ENDPOINT;

@Component
public class InMemoryCredentialStore implements CredentialStore, CredentialRefreshListener {

    private final Map<Long, Credential> credentialMap = new HashMap<>();

    // refresh token as key
    private final Map<String, String> tokenMap = new ConcurrentHashMap<>();

    private final Logger logger = LoggerFactory.getLogger(InMemoryCredentialStore.class);

    @Override
    public void writeCredential(Long userId, Credential credential) {
        credentialMap.put(userId, credential);
        tokenMap.put(credential.getRefreshToken(), credential.getAccessToken());
    }

    @Override
    public Optional<Credential> getCredential(Long userId) {
        return credentialMap.containsKey(userId) ? Optional.of(credentialMap.get(userId)) : Optional.empty();
    }

    @Override
    public void onTokenResponse(Credential credential, TokenResponse tokenResponse) throws IOException {
        String refreshToken = credential.getRefreshToken();
        String accessToken = credential.getAccessToken();
        String revokedAccessToken = tokenMap.get(credential.getRefreshToken());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(REVOKE_TOKEN_ENDPOINT + revokedAccessToken))
                                         .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
              .thenAccept(response -> {
                  if (response.statusCode() == 200) {
                      logger.info("successfully revoked access token");
                      tokenMap.replace(refreshToken, accessToken);
                  }
              });
        System.out.println("refreshed: " + credential);
    }

    @Override
    public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) throws IOException {

    }

    @PreDestroy
    public void onExit() {
        HttpClient client = HttpClient.newHttpClient();
        tokenMap.forEach((refreshToken, accessToken) -> {
            HttpRequest request1 = HttpRequest.newBuilder(URI.create(REVOKE_TOKEN_ENDPOINT + refreshToken))
                                              .build();

            HttpRequest request2 = HttpRequest.newBuilder(URI.create(REVOKE_TOKEN_ENDPOINT + accessToken))
                                              .build();
            CompletableFuture<Void> future1 = client.sendAsync(request1, HttpResponse.BodyHandlers.ofString())
                                              .thenAccept(response -> {
                                                  if (response.statusCode() == 200) {
                                                      logger.info("successfully revoked refresh token {}", refreshToken);
                                                  }
                                              });

            CompletableFuture<Void> future2 = client.sendAsync(request2, HttpResponse.BodyHandlers.ofString())
                                                    .thenAccept(response -> {
                                                        if (response.statusCode() == 200) {
                                                            logger.info("successfully revoked access token {}", accessToken);
                                                        }
                                                    });

            CompletableFuture.allOf(future1, future2).join();
        });
    }
}
