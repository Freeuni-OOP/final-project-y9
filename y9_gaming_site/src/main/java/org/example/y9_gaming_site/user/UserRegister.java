package org.example.y9_gaming_site.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserRegister {

    private final UserService userService;

    public UserRegister(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterDto registerDto) {
        try {
            if (registerDto.getUsername() == null || registerDto.getUsername().isBlank()) {
                return ResponseEntity.badRequest().body("Username is required");
            }
            if (registerDto.getEmail() == null || registerDto.getEmail().isBlank()) {
                return ResponseEntity.badRequest().body("Email is required");
            }
            if (registerDto.getPassword() == null || registerDto.getPassword().length() < 8) {
                return ResponseEntity.badRequest().body("Password must be at least 8 characters long");
            }

            userService.addNewUser(registerDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }
}