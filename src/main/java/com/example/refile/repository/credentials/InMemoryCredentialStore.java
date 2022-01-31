package com.example.refile.repository.credentials;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryCredentialStore implements CredentialStore, CredentialRefreshListener {

    private final Map<Long, Credential> credentialMap = new HashMap<>();

    @Override
    public void writeCredential(Long userId, Credential credential) {
        credentialMap.put(userId, credential);
    }

    @Override
    public Credential getCredential(Long userId) {
        return credentialMap.get(userId);
    }

    @Override
    public void onTokenResponse(Credential credential, TokenResponse tokenResponse) throws IOException {
        System.out.println("refreshed: " + credential);
    }

    @Override
    public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) throws IOException {

    }
}
