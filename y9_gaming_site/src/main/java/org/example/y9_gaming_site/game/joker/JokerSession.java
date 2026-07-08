package org.example.y9_gaming_site.game.joker;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.y9_gaming_site.user.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "joker_sessions")
@Getter
@Setter
public class JokerSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @ManyToOne
    @JoinColumn(name="host_id", nullable = false)
    private User host;

    @Column(name = "room_code", nullable = false, unique = true)
    private String roomCode;

    @Column(name = "status", nullable = false)
    private String status = "WAITING";

    @Column(name = "player_count", nullable = false)
    private int playerCount;

    @Column(name = "total_rounds", nullable = false)
    private int totalRounds;

    @Column(name = "is_open")
    private boolean isOpen;

    @Column(name = "joker_amount")
    private int jokerAmount;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    public JokerSession() {}

    public JokerSession(User host, String roomCode, int playerCount, int totalRounds, boolean isOpen, int jokerAmount) {
        this.host=host;
        this.roomCode = roomCode;
        this.playerCount = playerCount;
        this.totalRounds = totalRounds;
        this.isOpen = isOpen;
        this.jokerAmount = jokerAmount;
    }
}
