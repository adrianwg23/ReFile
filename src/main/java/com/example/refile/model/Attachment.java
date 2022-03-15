package com.example.refile.model;

import com.example.refile.dto.View;
import com.example.refile.util.StringSetConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

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
    @JsonView(View.Public.class)
    private Long attachmentId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonView(View.Public.class)
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", updatable = false)
    @JsonView(View.Public.class)
    private Date createdDate;

    @Convert(converter = StringSetConverter.class)
    @Column(name = "labelIds", length=500)
    @JsonView(View.Public.class)
    private Set<String> labelIds = new HashSet<>();

    @Convert(converter = StringSetConverter.class)
    @Column(name = "categories", length=1000)
    @JsonView(View.Public.class)
    private Set<String> categories = new HashSet<>();

    @Convert(converter = StringSetConverter.class)
    @Column(name = "cc", length=500000)
    @JsonView(View.Public.class)
    private Set<String> cc = new HashSet<>();

    @Convert(converter = StringSetConverter.class)
    @Column(name = "receiver", length=500000)
    @JsonView(View.Public.class)
    private Set<String> receiver = new HashSet<>();

    @JsonIgnore
    @Column(name = "g_id", length = 1000)
    private String gId;

    @Column(name = "name", length=259)
    @JsonView(View.Public.class)
    private String name;

    @JsonView(View.Public.class)
    private String extension;

    @Column(name = "sender", length=261)
    @JsonView(View.Public.class)
    private String sender;

    @Column(name = "senderEmail", length=262)
    @JsonView(View.Public.class)
    private String senderEmail;

    @Column(name = "thread", length=263)
    @JsonView(View.Public.class)
    private String thread;

    @Column(name = "subject", length=264)
    @JsonView(View.Public.class)
    private String subject;

    @Column(name = "importance", length=265)
    @JsonView(View.Public.class)
    private String importance;

    @JsonView(View.Data.class)
    @Column(name = "body", length = 1000000)
    private String body;
}
