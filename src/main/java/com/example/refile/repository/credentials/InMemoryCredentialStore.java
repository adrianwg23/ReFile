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

@Component
public class InMemoryCredentialStore implements CredentialStore, CredentialRefreshListener {

    private final Map<Long, Credential> idToCredentialMap = new HashMap<>();
    private final Map<Credential, Long> credentialToIdMap = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(InMemoryCredentialStore.class);

    @Override
    public void writeCredential(Long userId, Credential credential) {
        idToCredentialMap.put(userId, credential);
        credentialToIdMap.put(credential, userId);
    }

    @Override
    public Optional<Credential> getCredential(Long userId) {
        return idToCredentialMap.containsKey(userId) ? Optional.of(idToCredentialMap.get(userId)) : Optional.empty();
    }

    @Override
    public void onTokenResponse(Credential credential, TokenResponse tokenResponse) throws IOException {
        logger.info("refreshed access token for user id {}", credentialToIdMap.get(credential));
    }

    @Override
    public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) throws IOException {

    }

    @PreDestroy
    public void onExit() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        idToCredentialMap.forEach((k, v) -> futures.add(TokenUtil.revokeToken(v.getAccessToken())));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
