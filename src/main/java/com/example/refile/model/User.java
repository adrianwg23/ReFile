package com.example.refile.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue
    private Long id;

    private String email;

    public User(String email) {
        this.email = email;
    }

}
