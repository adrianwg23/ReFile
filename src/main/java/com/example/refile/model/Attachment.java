package com.example.refile.model;

import com.example.refile.util.StringSetConverter;
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
    @Column(name = "label_ids")
    private Set<String> labelIds = new HashSet<>();

    @Convert(converter = StringSetConverter.class)
    @Column(name = "categories")
    private Set<String> categories = new HashSet<>();

    @Column(name = "g_id", length = 1000)
    private String gId;

    private String name;
    private String extension;
    private String sender;
    private String thread;
}
