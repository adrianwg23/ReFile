package com.example.refile.util;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class HttpUtil {

    public static HttpTransport getHttpTransport() {
        try {
            return GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
