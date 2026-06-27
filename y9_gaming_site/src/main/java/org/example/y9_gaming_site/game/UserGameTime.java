package org.example.y9_gaming_site.game;

import jakarta.persistence.*;
import org.example.y9_gaming_site.user.User;

@Entity
@Table(name = "user_game_time")
public class UserGameTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "game_title", nullable = false)
    private String gameTitle;

    @Column(name = "total_time_seconds", nullable = false)
    private long totalTimeSeconds;

    @Column(name = "category", nullable = false)
    private String category;

    public UserGameTime() {}

    public UserGameTime(User user, String gameTitle, long totalTimeSeconds) {
        this.user = user;
        this.gameTitle = gameTitle;
        this.totalTimeSeconds = totalTimeSeconds;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getGameTitle() { return gameTitle; }
    public void setGameTitle(String gameTitle) { this.gameTitle = gameTitle; }

    public long getTotalTimeSeconds() { return totalTimeSeconds; }
    public void setTotalTimeSeconds(long totalTimeSeconds) { this.totalTimeSeconds = totalTimeSeconds; }

    public void setCategory(String category) { this.category = category;
    }
    public String getCategory() {
        return category;
    }
}