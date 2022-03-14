package com.example.refile.service;

import com.example.refile.dto.SenderDto;
import com.example.refile.dto.ThreadDto;
import com.example.refile.model.Attachment;
import com.example.refile.model.User;
import com.example.refile.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public List<Attachment> getTop1000AttachmentsByUser(User user) {
        return attachmentRepository.findTop1000ByUserOrderByCreatedDateDesc(user);
    }

    public List<Attachment> getAllAttachments() {
        return attachmentRepository.findAll();
    }

    public List<SenderDto> getTopSenders(User user) {
        Set<SenderDto> senders = new HashSet<>();
        List<Attachment> attachments = attachmentRepository.findTop100ByUserOrderByCreatedDateDesc(user);
        for (Attachment attachment : attachments) {
            senders.add(new SenderDto(attachment.getSenderEmail()));
            if (senders.size() == 3) {
                break;
            }
        }

        return new ArrayList<>(senders);
    }

    public List<ThreadDto> getTopThreads(User user) {
        Set<ThreadDto> threads = new HashSet<>();
        List<Attachment> attachments = attachmentRepository.findTop100ByUserOrderByCreatedDateDesc(user);
        for (Attachment attachment : attachments) {
            threads.add(new ThreadDto(attachment.getThread()));
            if (threads.size() == 6) {
                break;
            }
        }

        return new ArrayList<>(threads);
    }
}
