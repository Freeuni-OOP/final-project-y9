package org.example.y9_gaming_site.user;




import org.example.y9_gaming_site.security.ContentModerator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;




@Service
public class UserService {
    private final UserRepository userRepository;
    private final Random random = new Random();
    private final BCryptPasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {


        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }




    public User addNewUser(User user) throws Exception {
        if (ContentModerator.isFlagged(user.getUsername())) {
            throw new IllegalArgumentException("Username contains inappropriate language or violates safety guidelines.");
        }
        String passwordRegex = "^(?=.*[@#$%^&+=!*?<>/'{}])(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z]).{8,}$";
        if (user.getPassword() == null || !user.getPassword().matches(passwordRegex)) {
            throw new Exception("Password must be at least 8 characters long. It must contain at least one number, one uppercase letter and one lowercase letter.");
        }
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            List<String> usernameSuggestions = generateSuggestions(user.getUsername());
            throw new Exception("This Username is already taken, you can try one of these: " + String.join(", ", usernameSuggestions));
        }
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }




    private List<String> generateSuggestions(String name) {
        List<String> suggestions = new ArrayList<>();
        while (suggestions.size() < 3) {
            String option;
            int style = random.nextInt(3);
            if (style == 0) {
                option = name + random.nextInt(1000);
            } else if (style == 1) {
                option = name + "_" + random.nextInt(100);
            } else {
                String tmp = generateAdjectives();
                option = tmp + name;
            }
            if (!userRepository.findByUsername(option).isPresent() && !suggestions.contains(option)) {
                suggestions.add(option);
            }
        }
        return suggestions;
    }




    private String generateAdjectives() {
        String[] adjectives = {
                "Epic", "Shadow", "Cyber", "Cosmic", "Ghost",
                "TheReal", "Amazing", "TheOneAndOnly"
        };
        int randomIndex = random.nextInt(adjectives.length);
        return adjectives[randomIndex];
    }
}


