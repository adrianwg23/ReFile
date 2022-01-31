package com.example.refile.config;

import com.example.refile.service.CredentialService;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Configuration
public class GoogleConfig {

    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    @Bean
    public GoogleClientSecrets getClientSecrets() throws IOException {
        // Load client secrets.
        InputStream in = CredentialService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        return GoogleClientSecrets
                .load(GsonFactory.getDefaultInstance(), new InputStreamReader(in));
    }
}
