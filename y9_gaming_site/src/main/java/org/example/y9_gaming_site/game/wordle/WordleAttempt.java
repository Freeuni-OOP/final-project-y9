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

    @Column(name = "hinted_positions", nullable = false, length = 32, columnDefinition = "VARCHAR(32) NOT NULL DEFAULT ''")
    private String hintedPositions = "";

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

    public List<Integer> getHintedPositionList() {
        if(hintedPositions == null || hintedPositions.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> result = new ArrayList<>();
        for (String s : hintedPositions.split(",")) {
            result.add(Integer.parseInt(s));
        }
        return result;
    }

    public void addHintedPosition(int position) {
        List<Integer> current = getHintedPositionList();
        if (current.contains(position)) return;
        current.add(position);
        List<String> asStrings = new ArrayList<>();
        for (int p : current) asStrings.add(String.valueOf(p));
        this.hintedPositions = String.join(",", asStrings);
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}

    public WordlePuzzle getPuzzle() {return puzzle;}
    public void setPuzzle(WordlePuzzle puzzle) {this.puzzle = puzzle;}

    public String getGuesses() {return guesses;}
    public void setGuesses(String guesses) {this.guesses = guesses;}

    public String getHintedPositions() {return hintedPositions;}
    public void setHintedPositions(String hintedPositions) {this.hintedPositions = hintedPositions;}

    public AttemptStatus getStatus() {return status;}
    public void setStatus(AttemptStatus status) {this.status = status;}

    public LocalDateTime getStartedAt() {return startedAt;}
    public void setStartedAt(LocalDateTime startedAt) {this.startedAt = startedAt;}

    public LocalDateTime getCompletedAt() {return completedAt;}
    public void setCompletedAt(LocalDateTime completedAt) {this.completedAt = completedAt;}


}
