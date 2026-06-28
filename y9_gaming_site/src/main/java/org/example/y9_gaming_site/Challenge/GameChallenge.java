package org.example.y9_gaming_site.Challenge;

import jakarta.persistence.*;
import org.example.y9_gaming_site.gameRecord.GameRecord;
import org.example.y9_gaming_site.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_challenges")
public class GameChallenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "targ_record_id", nullable = false)
    private GameRecord targRecord;

    @ManyToOne
    @JoinColumn(name = "res_record_id", nullable = false)
    private GameRecord resRecord;

    @ManyToOne
    @JoinColumn(name = "winner_id", nullable = false)
    private User winner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameChallengeStatus status = GameChallengeStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt =  LocalDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public GameChallenge() {}

    public GameChallenge(User sender, User receiver, GameRecord targRecord, LocalDateTime expiresAt) {
        this.sender = sender;
        this.receiver = receiver;
        this.targRecord = targRecord;
        this.expiresAt = expiresAt;
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public User getSender() {return sender;}
    public void setSender(User sender) {this.sender = sender;}

    public User getReceiver() {return receiver;}
    public void setReceiver(User receiver) {this.receiver = receiver;}

    public GameRecord getTargRecord() {return targRecord;}
    public void setTargRecord(GameRecord targRecord) {this.targRecord = targRecord;}

    public GameRecord getResRecord() {return resRecord;}
    public void setResRecord(GameRecord resRecord) {this.resRecord = resRecord;}

    public User getWinner() {return winner;}
    public void setWinner(User winner) {this.winner = winner;}

    public GameChallengeStatus getStatus() {return status;}
    public void setStatus(GameChallengeStatus status) {this.status = status;}

    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public LocalDateTime getExpiresAt() {return expiresAt;}
    public void setExpiresAt(LocalDateTime expiresAt) {this.expiresAt = expiresAt;}

    public LocalDateTime getResolvedAt() {return resolvedAt;}
    public void setResolvedAt(LocalDateTime resolvedAt) {this.resolvedAt = resolvedAt;}

}
