package com.example.refile.repository;

import com.example.refile.model.Attachment;
import com.example.refile.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findAllByUserOrderByCreatedDateDesc(User user);
    List<Attachment> findTop1000ByUserOrderByCreatedDateDesc(User user);
}
