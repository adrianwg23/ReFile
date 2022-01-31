package com.example.refile.repository.credentials;

import com.google.api.client.auth.oauth2.Credential;

public interface CredentialStore {

    public void writeCredential(Long userId, Credential credential);

    public Credential getCredential(Long userId);
}
