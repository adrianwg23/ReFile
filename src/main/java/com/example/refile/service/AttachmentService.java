package com.example.refile.service;

import com.example.refile.model.Attachment;
import com.example.refile.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    public Attachment putAttachment(Attachment attachment) {
        return attachmentRepository.save(attachment);
    }

    public void deleteAttachment(Attachment attachment) {
        attachmentRepository.deleteById(attachment.getAttachmentId());
    }
}
