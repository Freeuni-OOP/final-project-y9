package org.example.y9_gaming_site.game.wordle;


import jakarta.persistence.*;
import org.example.y9_gaming_site.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "wordle_attempts", uniqueConstraints = @UniqueConstraint(name = "unique_user_puzzle",
columnNames = {"user_id", "puzzle_id"}))
public class WordleAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "puzzle_id", nullable = false)
    private WordlePuzzle puzzle;

    @Column(name = "guesses", nullable = false, length = 64)
    private String guesses = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public WordleAttempt() {}

    public WordleAttempt(User user, WordlePuzzle puzzle) {
        this.user = user;
        this.puzzle = puzzle;
    }
    public List<String> getGuessList() {
        if(guesses == null || guesses.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(guesses.split(",")));
    }

    public void addGuess(String guess) {
        List<String> currr = getGuessList();
        currr.add(guess);
        this.guesses = String.join(",", currr);
    }

    public int guessCount() {
        return getGuessList().size();
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}

    public WordlePuzzle getPuzzle() {return puzzle;}
    public void setPuzzle(WordlePuzzle puzzle) {this.puzzle = puzzle;}

    public String getGuesses() {return guesses;}
    public void setGuesses(String guesses) {this.guesses = guesses;}

    public AttemptStatus getStatus() {return status;}
    public void setStatus(AttemptStatus status) {this.status = status;}

    public LocalDateTime getStartedAt() {return startedAt;}
    public void setStartedAt(LocalDateTime startedAt) {this.startedAt = startedAt;}

    public LocalDateTime getCompletedAt() {return completedAt;}
    public void setCompletedAt(LocalDateTime completedAt) {this.completedAt = completedAt;}


}
