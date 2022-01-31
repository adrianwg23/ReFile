package com.example.refile.repository.credentials;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InMemoryCredentialStore implements CredentialStore {

    private Map<Long, Credential> credentialMap = new HashMap<>();

    @Override
    public void writeCredential(Long userId, Credential credential) {
        credentialMap.put(userId, credential);
    }

    @Override
    public Credential getCredential(Long userId) {
        return null;
    }
}
