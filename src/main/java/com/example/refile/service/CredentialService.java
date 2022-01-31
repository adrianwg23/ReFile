package com.example.refile.service;

import com.example.refile.repository.credentials.CredentialStore;
import com.example.refile.repository.credentials.InMemoryCredentialStore;
import com.example.refile.util.HttpUtil;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CredentialService {

    private final GoogleClientSecrets googleClientSecrets;
    private final CredentialStore credentialStore;

    @SneakyThrows
    public void saveCredential(Long userId, String accessToken, String refreshToken) {
        Credential credential = new GoogleCredential.Builder()
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setTransport(HttpUtil.getHttpTransport())
                .setClientSecrets(googleClientSecrets)
                .setRefreshListeners(Collections.singleton((InMemoryCredentialStore) credentialStore))
                .build()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)
                .setExpiresInSeconds(3600L);

        credentialStore.writeCredential(userId, credential);
    }

    public Optional<Credential> getCredential(Long userId) {
        return credentialStore.getCredential(userId);
    }
}
