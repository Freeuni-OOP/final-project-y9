package org.example.y9_gaming_site.user;

import org.example.y9_gaming_site.dto.AvatarUploadResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    //used by navbar to show avatar of user
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("not logged in");
        }
        User user = (User) authentication.getPrincipal();
        String userName= user.getUsername();
        try{
            return ResponseEntity.ok(userService.getProfileByUsername(userName));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("avatar") MultipartFile avatar) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in");
        }
        User user = (User) authentication.getPrincipal();
        String userName= user.getUsername();

        try {
            String avatarUrl = userService.updateOrCreateAvatar(userName, avatar);
            return ResponseEntity.ok(new AvatarUploadResponse(avatarUrl, "Updated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}