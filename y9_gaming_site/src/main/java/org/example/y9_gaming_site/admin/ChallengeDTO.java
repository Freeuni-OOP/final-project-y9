package org.example.y9_gaming_site.admin;

public class ChallengeDTO {

    private String title;
    private String description;
    private String reward;

    public ChallengeDTO() {}

    public ChallengeDTO(String title, String description, String reward) {
        this.title = title;
        this.description = description;
        this.reward = reward;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReward() { return reward; }
    public void setReward(String reward) { this.reward = reward; }
}