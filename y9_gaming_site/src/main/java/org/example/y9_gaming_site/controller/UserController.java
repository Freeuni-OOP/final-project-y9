package org.example.y9_gaming_site.controller;

import jakarta.servlet.http.HttpSession;
import org.example.y9_gaming_site.dto.AvatarUploadResponse;
import org.example.y9_gaming_site.dto.UserProfileResponse;
import org.example.y9_gaming_site.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @getMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUser(@PathVariable Long id){
        return ResponseEntity.ok(userService.getProfile(id));
    }

    @PostMapping("/avatar")
    public ResponseEntity<AvatarUploadResponse> uploadAvatar(@RequestParam("avatar") MultipartFile avatar, HttpSession session){
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null){
            throw new RuntimeException("userId is null");
        }
        String newAvatarUrl = userService.updateOrCreateAvatar(userId, avatar);
        return ResponseEntity.ok(new AvatarUploadResponse(newAvatarUrl, "Photo updated"));
    }
}
