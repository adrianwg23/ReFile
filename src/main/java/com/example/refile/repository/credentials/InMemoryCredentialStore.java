package com.example.refile.repository.credentials;

import com.example.refile.util.TokenUtil;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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

        TokenUtil.revokeToken(revokedAccessToken);
        tokenMap.replace(refreshToken, accessToken);
    }

    @Override
    public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) throws IOException {

    }

    @PreDestroy
    public void onExit() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        tokenMap.forEach((refreshToken, accessToken) -> {
            futures.add(TokenUtil.revokeToken(accessToken));
        });
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
