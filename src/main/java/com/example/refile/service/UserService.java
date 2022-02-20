package com.example.refile.service;

import com.example.refile.model.User;
import com.example.refile.repository.UserRepository;
import com.example.refile.util.TokenUtil;
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
            User user = User.builder()
                    .name(name)
                    .email(email)
                    .build();
            userRepository.save(user);
            return user;
        }

        return optionalUser.get();
    }

    public void setRefreshToken(User user, String refreshToken) {
        if (user.getRefreshToken() != null) {
            TokenUtil.revokeToken(refreshToken);
        }
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
    }
}
