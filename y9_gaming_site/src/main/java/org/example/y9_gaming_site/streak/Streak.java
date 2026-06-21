package org.example.y9_gaming_site.streak;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "streaks")
@Getter
@Setter
public class Streak {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;          // whose streak is this
    private Integer currentStreak; // how many
    private LocalDate lastLogin;
}