package com.example.refile.model;

import com.example.refile.util.StringSetConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Builder
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="attachments")
public class Attachment {

    @Id
    @GeneratedValue
    @Column(name = "attachment_id")
    private Long attachmentId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", updatable = false)
    private Date createdDate;

    @Convert(converter = StringSetConverter.class)
    @Column(name = "label_ids", length = 300)
    private Set<String> labelIds = new HashSet<>();

    @Convert(converter = StringSetConverter.class)
    @Column(name = "categories", length = 301)
    private Set<String> categories = new HashSet<>();

    @Convert(converter = StringSetConverter.class)
    @Column(name = "cc", length = 302)
    private Set<String> cc = new HashSet<>();

    @Convert(converter = StringSetConverter.class)
    @Column(name = "receiver", length=256)
    private Set<String> receiver = new HashSet<>();

    @JsonIgnore
    @Column(name = "g_id", length = 1000)
    private String gId;

    @Column(name = "name", length=257)
    private String name;
    @Column(name = "extension", length=258)
    private String extension;
    @Column(name = "sender", length=259)
    private String sender;
    @Column(name = "senderEmail", length=260)
    private String senderEmail;
    @Column(name = "thread", length=261)
    private String thread;
    @Column(name = "subject", length=262)
    private String subject;
    @Column(name = "importance", length=263)
    private String importance;
    @Column(name = "body", length = 65535)
    private String body;

    @Column(length = 500)
    private String snippet;
}
