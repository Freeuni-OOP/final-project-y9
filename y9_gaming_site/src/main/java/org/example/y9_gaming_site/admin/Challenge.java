package org.example.y9_gaming_site.admin;


import jakarta.persistence.*;
import org.example.y9_gaming_site.user.User;

import java.time.LocalDateTime;
import java.util.Optional;


@Entity
@Table(name = "challenges")
public class Challenge {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    //private Long admin_id;
    private String title;
    private String description;
    private String reward;
    private LocalDateTime createdAt;
    private Long admin_id;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "admin_id", nullable = false) // Maps to 'admin_id' column, but points to User entity
//    private User admin; // Changed from Admin to User

//    public User getAdmin() { return admin; }
//    public void setAdmin(User admin) { this.admin = admin; }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAdmin_id() { return admin_id;}
    public void setAdmin_id(Long admin_id){this.admin_id = admin_id;}


    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }


    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


    public String getReward() { return reward; }
    public void setReward(String reward) { this.reward = reward; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

