package com.example.refile.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class TokenUtil {

    private static final String REVOKE_TOKEN_ENDPOINT = "https://accounts.google.com/o/oauth2/revoke?token=";
    private static final Logger logger = LoggerFactory.getLogger(TokenUtil.class);

    public static CompletableFuture<Void> revokeToken(String token) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(REVOKE_TOKEN_ENDPOINT + token))
                                         .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                     .thenAccept(response -> {
                         if (response.statusCode() == 200) {
                             logger.info("successfully revoked token {}", token);
                         } else {
                             logger.info("failed to revoke token with error code {}", response.statusCode());
                             logger.info(response.body());
                         }
                     });
    }
}
