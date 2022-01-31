package com.example.refile.repository.credentials;

import com.google.api.client.auth.oauth2.Credential;

import java.util.Optional;

public interface CredentialStore {

    void writeCredential(Long userId, Credential credential);

    Optional<Credential> getCredential(Long userId);
}
