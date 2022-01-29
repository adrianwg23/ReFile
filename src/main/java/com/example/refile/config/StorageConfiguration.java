package com.example.refile.config.auth;

import com.google.auth.Credentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfiguration {

    private static final String GCS_PROJCET_ID = "refile-338520";

    @Bean
    public Storage getStorage(Credentials credentials) {
        return StorageOptions.newBuilder().setCredentials(credentials)
                                               .setProjectId(GCS_PROJCET_ID).build().getService();
    }
}
