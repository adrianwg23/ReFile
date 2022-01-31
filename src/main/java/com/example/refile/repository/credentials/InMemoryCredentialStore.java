package com.example.refile.repository.credentials;

import com.google.api.client.auth.oauth2.Credential;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryCredentialStore implements CredentialStore {

    private final Map<Long, Credential> credentialMap = new HashMap<>();

    @Override
    public void writeCredential(Long userId, Credential credential) {
        credentialMap.put(userId, credential);
    }

    @Override
    public Credential getCredential(Long userId) {
        return credentialMap.get(userId);
    }
}
