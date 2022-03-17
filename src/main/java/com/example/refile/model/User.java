package com.example.refile.model;

import com.example.refile.util.StringSetConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.*;

@Builder
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="users")
public class User {

    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private Long userId;

    @JsonIgnore
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy(value = "created_date desc")
    private List<Attachment> attachments = new ArrayList<>();

    @JsonIgnore
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", updatable = false)
    private Date createdDate;

    private String email;

    private String name;

    @JsonIgnore
    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @Convert(converter = StringSetConverter.class)
    @Column(name = "categories")
    private Set<String> categories = Set.of("Contract", "Invoice", "Receipt");

    public User(String email, String name) {
        this.email = email;
        this.name = name;
    }
}
