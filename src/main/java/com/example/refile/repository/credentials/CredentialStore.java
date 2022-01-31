package com.example.refile.repository.credentials;

import com.google.api.client.auth.oauth2.Credential;

public interface CredentialStore {

    void writeCredential(Long userId, Credential credential);

    Credential getCredential(Long userId);
}
