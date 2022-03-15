package com.example.refile.service;

import com.example.refile.model.Attachment;
import com.example.refile.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CategorizationService {

    private static final String URL = "https://us-east1-refile-338520.cloudfunctions.net/refile-ml-v1-for-real";
    private static final Logger logger = LoggerFactory.getLogger(CategorizationService.class);

    private final UserService userService;
    private final AttachmentService attachmentService;

    public List<String> extractCategories(String text, Set<String> categories, Set<String> seenCategories) {
        if (text == null) {
            return new ArrayList<>();
        }
        String lowerCaseText = text.toLowerCase();
        List<String> containedCategories = new ArrayList<>();

        categories.stream()
                  .filter(category -> !seenCategories.contains(category))
                  .forEach(category -> {
                      if (lowerCaseText.contains(category.toLowerCase())) {
                          containedCategories.add(category);
                      }
                  });

        return containedCategories;
    }

    public CompletableFuture<Void> clusterAttachments(User user) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(URL))
                                         .header("Content-Type", "application/json")
                                         .POST(HttpRequest.BodyPublishers.ofString(String.format("{\"user_id\": %d}", user.getUserId())))
                                         .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
              .thenAccept(response -> {
                  if (response.statusCode() == 200) {
                      logger.info("success");
                      String body = response.body();
                      ObjectMapper mapper = new ObjectMapper();
                      try {
                          Map<String,Object> map = mapper.readValue(body, Map.class);
                          Map<String, List<Integer>> clusters = (HashMap) map.get("attachmentId_x");
                          Set<String> categories = new HashSet<>(user.getCategories());

                          clusters.forEach((cluster, attachments) -> {
                              int clusterNumber = Integer.parseInt(cluster) + 1;
                              String clusterName = "✨ Group " + clusterNumber;
                              categories.add(clusterName);

                              attachments.forEach(attachmentId -> {
                                  Attachment attachment = attachmentService.getAttachmentById(Long.valueOf(attachmentId));
                                  attachment.getCategories().add(clusterName);
                                  attachmentService.saveAttachment(attachment);
                              });

                              user.setCategories(categories);
                              userService.saveUser(user);
                              logger.info("finished persisting");
                          });
                      } catch (JsonProcessingException e) {
                          e.printStackTrace();
                      }
                  } else {
                      logger.info("fail");
                      logger.info(response.body());
                  }
              });
    }
}
