package org.example.y9_gaming_site.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChallengeDTO {

    private String title;
    private String description;
    private String reward;
    @JsonProperty("admin_id")
    private Long admin_id;

    public ChallengeDTO() {}

    public ChallengeDTO(String title, String description, String reward,Long admin_id) {
        this.title = title;
        this.description = description;
        this.reward = reward;

        this.admin_id = admin_id;

    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReward() { return reward; }
    public void setReward(String reward) { this.reward = reward; }

    public Long getAdmin_id(){return admin_id;}
    public void setAdmin_id(Long admin_id){this.admin_id = admin_id;}
}