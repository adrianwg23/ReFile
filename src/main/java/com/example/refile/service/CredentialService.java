package com.example.refile.service;

import com.example.refile.repository.credentials.CredentialStore;
import com.example.refile.util.HttpUtil;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
@RequiredArgsConstructor
public class CredentialService {

    private final CredentialStore credentialStore;

    public void saveCredential(Long userId, String accessToken, String refreshToken) {
        Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setTransport(HttpUtil.getHttpTransport())
                .build()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken);

        credentialStore.writeCredential(userId, credential);
    }

    public Credential getCredential(Long userId) {
        return credentialStore.getCredential(userId);
    }
}
