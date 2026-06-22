package org.example.y9_gaming_site.user;

import org.example.y9_gaming_site.dto.AvatarUploadResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody UserRegisterDto dto) {
        try {
            userService.addNewUser(dto);
            return ResponseEntity.ok(Map.of("message", "Registered successfully!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(userService.getProfile(id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("avatar") MultipartFile avatar) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            String avatarUrl = userService.updateOrCreateAvatar(Long.valueOf(authentication.getName()), avatar);
            return ResponseEntity.ok(new AvatarUploadResponse(avatarUrl, "Updated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}