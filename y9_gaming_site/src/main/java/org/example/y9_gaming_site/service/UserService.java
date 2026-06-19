package org.example.y9_gaming_site.service;


import org.example.y9_gaming_site.dto.UserProfileResponse;
import org.example.y9_gaming_site.user.UserRepository;
import org.example.y9_gaming_site.user.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserService{
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public UserService(UserRepository userRepository, FileStorageService fileStorageService){
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    public UserProfileResponse getProfile(Long id){
        User user = userRepository.findById(id).orElse(null);
        assert user != null;
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getAvatarUrl());
    }

    public String updateOrCreateAvatar(Long userId, MultipartFile avatar){
        User user = userRepository.findById(userId).orElse(null);
        String avatarUrl = fileStorageService.store(avatar);
        assert user != null;
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        return avatarUrl;
    }
}
