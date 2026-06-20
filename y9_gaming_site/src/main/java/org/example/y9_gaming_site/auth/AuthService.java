package org.example.y9_gaming_site.auth;

import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository=userRepository;
    }

    public User login(String userName, String password){
        User user = userRepository.findByEmail(userName).orElseThrow(
                () -> new RuntimeException("User not found"));
        if(!password.equals(user.getPassword())){
            throw new RuntimeException("Wrong password");
        }
        return user;
    }

}
