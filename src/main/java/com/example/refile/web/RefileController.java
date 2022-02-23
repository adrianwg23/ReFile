package com.example.refile.web;

import com.example.refile.model.Attachment;
import com.example.refile.model.CategoryDTO;
import com.example.refile.model.User;
import com.example.refile.service.GmailService;
import com.example.refile.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
public class RefileController {

    private final UserService userService;
    private final GmailService gmailService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> users(@PathVariable Long userId) {
        User user = userService.getUser(userId);
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
        return ResponseEntity.ok(gmailService.syncAttachments(user, true));
    }

    @GetMapping("/categories/{userId}")
    public ResponseEntity<?> getCategories(@PathVariable Long userId) {
        User user = userService.getUser(userId);
        return ResponseEntity.ok(user.getCategories());
    }

    @PostMapping("/categories/{userId}")
    public ResponseEntity<?> addCategory(@PathVariable Long userId, @RequestBody CategoryDTO categoryDTO) {
        User user = userService.getUser(userId);
        user.getCategories().add(categoryDTO.getCategory());
        userService.saveUser(user);
        return ResponseEntity.ok(user.getCategories());
    }
}
