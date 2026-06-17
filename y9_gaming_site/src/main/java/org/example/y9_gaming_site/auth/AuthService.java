package org.example.y9_gaming_site.auth;

import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String email, String password) {
        // ვეძებთ იუზერს email-ით
        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ვამოწმებთ პაროლს პირდაპირ (hashing-ს შემდეგ დავამატებთ)
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        return user;
    }
}
