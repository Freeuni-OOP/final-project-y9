package org.example.y9_gaming_site.dto;

public class UserProfileResponse {
    private Long id;
    private String username;
    private String avatarUrl;
    private String role;

    public UserProfileResponse(Long id, String username, String avatarUrl, String role) {
        this.id = id;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.role = role;
    }

    public Long getId() {
        return id;
    }
    public String getAvatarUrl(){return avatarUrl;}

    public String getUsername() {return username;}
    public String getRole() {return role;}
}
