package org.example.y9_gaming_site.dto;

/**
 *
 */

public class AvatarUploadResponse {
    private final String avatarUrl;
    private final String message;

    public AvatarUploadResponse(String avatarUrl, String message){
        this.avatarUrl = avatarUrl;
        this.message = message;
    }

    public String getAvatarUrl() {return avatarUrl;}

    public String getMessage() {return message;}
}
