package org.example.y9_gaming_site.auth;

import org.example.y9_gaming_site.security.TokenUtil;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public String login(String email, String password) {
        // ვეძებთ იუზერს email-ით
        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ვამოწმებთ პაროლს პირდაპირ (hashing-ს შემდეგ დავამატებთ)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        return TokenUtil.generateToken(user.getUsername());
    }
}
