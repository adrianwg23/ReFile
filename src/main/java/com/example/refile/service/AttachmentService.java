package com.example.refile.service;

import com.example.refile.model.Attachment;
import com.example.refile.model.User;
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

    public void deleteAllAttachments(List<Attachment> attachments) {
        attachmentRepository.deleteAllInBatch(attachments);
    }

    public Attachment getAttachmentById(Long id) {
        return attachmentRepository.findById(id).get();
    }

    public List<Attachment> getAttachmentsByUser(User user) {
        return attachmentRepository.findAllByUserOrderByCreatedDateDesc(user);
    }

    public List<Attachment> getAllAttachments() {
        return attachmentRepository.findAll();
    }
}
