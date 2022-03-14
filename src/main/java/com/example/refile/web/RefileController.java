package com.example.refile.web;

import com.example.refile.dto.CategoryDTO;
import com.example.refile.model.Attachment;
import com.example.refile.model.User;
import com.example.refile.service.AttachmentService;
import com.example.refile.service.GmailService;
import com.example.refile.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RefileController {

    private final UserService userService;
    private final AttachmentService attachmentService;
    private final GmailService gmailService;

    @Value("${app.api-key}")
    private String apiKey;

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> users(@PathVariable Long userId) {
        User user = userService.getUser(userId);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<User> updateCategories(@PathVariable Long userId, @RequestBody CategoryDTO categoryDTO) {
        User user = userService.getUser(userId);
        userService.setCategories(user, new HashSet<>(categoryDTO.getCategories()));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/attachments/{userId}")
    public ResponseEntity<List<Attachment>> attachments(@PathVariable Long userId) throws IOException {
        User user = userService.getUser(userId);
        return ResponseEntity.ok(gmailService.getAttachments(user));
    }

    @GetMapping("/sync-attachments/{userId}")
    public ResponseEntity<List<Attachment>> syncAttachments(@PathVariable Long userId) throws IOException {
        User user = userService.getUser(userId);
        return ResponseEntity.ok(gmailService.syncAttachments(user));
    }

    @GetMapping(value = "/gid/{attachmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> gid(@PathVariable Long attachmentId) {
        String gid = attachmentService.getAttachmentById(attachmentId).getGId();
        return ResponseEntity.ok(String.format("{\"gid\": \"%s\"}", gid));
    }

    @GetMapping(value = "/data/attachments/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> dataAttachments(@RequestHeader(value = "Authorization") String apiKey, @PathVariable Long userId) {
        if (apiKey == null || !apiKey.substring(7).equals(this.apiKey)) {
            return new ResponseEntity<>("{\"error\": \"Unauthorized\"}", HttpStatus.UNAUTHORIZED);
        }
        User user = userService.getUser(userId);

        return ResponseEntity.ok(attachmentService.getTop1000AttachmentsByUser(user));
    }
}
