package com.example.refile.model;

import com.example.refile.util.StringListConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
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

    @Convert(converter = StringListConverter.class)
    @Column(name = "label_ids")
    private List<String> labelIds;

    private String url;
    private String extension;
    private String sender;
    private String thread;
    private String subject;
}
