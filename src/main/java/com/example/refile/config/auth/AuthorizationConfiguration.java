package com.example.refile.config.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.Oauth2Scopes;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;

@Configuration
public class AuthorizationConfiguration {

    private static final String TOKENS_DIRECTORY = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String ADMIN_KEYS_FILE_PATH = "src/main/resources/refile-admin.json";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public static final String CALLBACK_URL = "http://localhost:8080/oauth2callback";

    @Bean
    public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow() throws IOException {
        // Load client secrets.
        InputStream in = AuthorizationConfiguration.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));


        return new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                clientSecrets,
                Arrays.asList(GmailScopes.MAIL_GOOGLE_COM, Oauth2Scopes.USERINFO_PROFILE, Oauth2Scopes.USERINFO_EMAIL))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY)))
                // Set the access type to offline so that the token can be refreshed.
                // By default, the library will automatically refresh tokens when it
                // can, but this can be turned off by setting
                // api.admanager.refreshOAuth2Token=false in your ads.properties file.
                .setAccessType("offline")
                .build();
    }

    @Bean
    public Credentials getAdminCredentials() throws IOException {
        return GoogleCredentials
                .fromStream(new FileInputStream(ADMIN_KEYS_FILE_PATH));
    }
}
