package com.example.refile.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Table(name="users")
public class User {

    @Id
    @GeneratedValue
    private Long userId;
    private String email;
    private String name;

    @JsonIgnore
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Attachment> attachments = new ArrayList<>();

    public User(String email, String name) {
        this.email = email;
        this.name = name;
    }
}
