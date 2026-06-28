package org.example.y9_gaming_site.gameRecord;


import jakarta.persistence.*;
import org.example.y9_gaming_site.game.Game;
import org.example.y9_gaming_site.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_records")
public class GameRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "context_id")
    private Long contextId;

    @Column(nullable = false)
    private double value;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    public GameRecord() {}

    public GameRecord(User user, Game game, Long contextId, double value, LocalDateTime recordedAt) {
        this.user = user;
        this.game = game;
        this.contextId = contextId;
        this.value = value;
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}

    public Game getGame() {return game;}
    public void setGame(Game game) {this.game = game;}

    public Long getContextId() {return contextId;}
    public void setContextId(Long contextId) {this.contextId = contextId;}

    public double getValue() {return value;}
    public void setValue(double value) {this.value = value;}

    public LocalDateTime getRecordedAt() {return recordedAt;}
    public void setRecordedAt(LocalDateTime recordedAt) {this.recordedAt = recordedAt;}
}
