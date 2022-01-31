package com.example.refile.web;

import com.example.refile.model.Attachment;
import com.example.refile.model.User;
import com.example.refile.service.GmailService;
import com.example.refile.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class RefileController {

    private final UserService userService;
    private final GmailService gmailService;

    @GetMapping("/attachments/{userId}")
    public ResponseEntity<List<Attachment>> attachments(@PathVariable Long userId) {
        User user = userService.getUser(userId);
        return ResponseEntity.ok(gmailService.getAttachments(user));
    }

    @PostMapping("sync-attachments")
    public void syncAttachments(@RequestParam Long userId) {
        User user = userService.getUser(userId);
        gmailService.syncAttachments(user);
    }
}
