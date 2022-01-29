package com.example.refile.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@Table(name="attachments")
public class Attachment {

    @Id
    @GeneratedValue
    private Long attachmentId;
    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;
}
