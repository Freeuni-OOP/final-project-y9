package org.example.y9_gaming_site.user;

import org.example.y9_gaming_site.dto.UserProfileResponse;
import org.example.y9_gaming_site.security.ContentModerator;
import org.example.y9_gaming_site.security.PasswordUtil;
import org.example.y9_gaming_site.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final Random random = new Random();
    private final FileStorageService fileStorageService;

    public UserService(UserRepository userRepository, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    public void addNewUser(UserRegisterDto dto) throws Exception {
        if (ContentModerator.isFlagged(dto.getUsername())) {
            throw new IllegalArgumentException("Username contains inappropriate language or violates safety guidelines.");
        }

        String passwordRegex = "^(?=.*[@#$%^&+=!*?<>/'{}])(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z]).{8,}$";
        if (dto.getPassword() == null || !dto.getPassword().matches(passwordRegex)) {
            throw new Exception("Password must be at least 8 characters long. It must contain at least one number, one uppercase letter, one lowercase letter, and one special character.");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            List<String> list = generateSuggestions(dto.getUsername());
            throw new IllegalArgumentException("Username is already taken. Similar available Options: " + String.join(", ", list));
        }

        if (dto.getBirthDate() == null) {
            throw new IllegalArgumentException("Birth date is required.");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(Role.USER);
        user.setBirthDate(dto.getBirthDate());

        String key = PasswordUtil.generateKey();
        user.setSalt(key);
        user.setPassword(PasswordUtil.hashPassword(dto.getPassword(), key));

        userRepository.save(user);
    }

    public User createGuestUser() {
        User guest = new User();
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        guest.setUsername("Guest_" + uniqueId);
        guest.setEmail(uniqueId + "@guest.y9gaming.local");

        String key = PasswordUtil.generateKey();
        guest.setSalt(key);
        guest.setPassword(PasswordUtil.hashPassword(UUID.randomUUID().toString(), key));

        guest.setBirthDate(LocalDate.of(1970, 1, 1));
        guest.setRole(Role.GUEST);

        return userRepository.save(guest);
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
            if (!userRepository.existsByUsername(option) && !suggestions.contains(option)) {
                suggestions.add(option);
            }
        }
        return suggestions;
    }

    private String generateAdjectives() {
        String[] adjectives = {
                "Epic", "Shadow", "Cyber", "Cosmic", "SixSeven",
                "TheReal", "Amazing", "TheOneAndOnly", "TheRealSlim",
                "TungTungTung"

        };
        int randomIndex = random.nextInt(adjectives.length);
        return adjectives[randomIndex];
    }

    public UserProfileResponse getProfile(Long id){
        User user = userRepository.findById(id).orElse(null);
        assert user != null;
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getAvatarUrl(), user.getRole().name());
    }

    public String updateOrCreateAvatar(String userName, MultipartFile avatar){
        User user = userRepository.findByUsername(userName).orElseThrow(() -> new RuntimeException("Username not found."));
        String avatarUrl = fileStorageService.store(avatar);
        assert user != null;
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        return avatarUrl;
    }


    //I added this function cause authentication returns only name
    public UserProfileResponse getProfileByUsername(String userName){
        User user = userRepository.findByUsername(userName).orElseThrow(() ->new IllegalArgumentException("Username not found."));
        return  new UserProfileResponse(user.getId(), user.getUsername(), user.getAvatarUrl(), user.getRole().name());
    }
}