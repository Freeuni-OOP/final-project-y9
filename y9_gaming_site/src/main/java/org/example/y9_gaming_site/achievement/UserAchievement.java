package org.example.y9_gaming_site.achievement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

@Entity
@Table(name="user_achievements")
@Getter
@Setter
//this class is connection between user and achievement.
public class UserAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id; //unique identifier

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId; //unique identifier of user who achieved

    @Column(name = "achievement_id", insertable = false, updatable = false)
    private Long achievementId; //unique identifier of achievement which was achieved
    private LocalDateTime earnedTime; //to know which was the latest achievement

}
