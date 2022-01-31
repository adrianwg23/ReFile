package com.example.refile.web;

import com.example.refile.model.Attachment;
import com.example.refile.service.GmailService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class RefileController {

    private final GmailService gmailService;

    @GetMapping("/attachments/{userId}")
    public ResponseEntity<List<Attachment>> attachments(@PathVariable Long userId) {
        return ResponseEntity.ok(gmailService.getAttachments(userId));
    }

    @PostMapping("sync-attachments")
    public void syncAttachments(@RequestParam Long userId) {
        gmailService.syncAttachments(userId);
    }
}
