package org.example.y9_gaming_site.leaderboard;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboard_scores")
@Getter
@Setter
//save info about every game played
public class LeaderboardScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  id;
    @Column(name="user_id", insertable = false, updatable = false)
    private Long userId;
    private String gameName;
    private Integer score;
    private LocalDateTime playedAt;
}
