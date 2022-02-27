package com.example.refile.service;

import com.example.refile.model.User;
import com.example.refile.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUser(Long id) {
        return userRepository.findById(id).get();
    }

    public User saveUser(User user) {
        return userRepository.save(user);
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

    public void setRefreshToken(User user, String refreshToken) {
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
    }

    public void setCategories(User user, Set<String> categories) {
        user.setCategories(categories);
        userRepository.save(user);
    }
}
