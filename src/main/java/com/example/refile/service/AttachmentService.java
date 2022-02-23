package com.example.refile.service;

import com.example.refile.model.Attachment;
import com.example.refile.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    public List<Attachment> saveAllAttachments(List<Attachment> attachments) {
        return attachmentRepository.saveAll(attachments);
    }

    public void deleteAllAttachments() {
        attachmentRepository.deleteAll();
    }
}
