package com.example.refile.service;

import com.example.refile.model.User;
import com.example.refile.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUser(Long id) {
        return userRepository.findById(id).get();
    }

    public User putUser(String email, String name) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            User user = new User(email, name);
            userRepository.save(user);
            return user;
        }

        return optionalUser.get();
    }
}
