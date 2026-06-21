package org.example.y9_gaming_site.streak;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class StreakService {
    private final StreakRepository streakRepository;

    public StreakService(StreakRepository streakRepository) {
        this.streakRepository = streakRepository;
    }

    //updating streak logic

    public void updateStreak(Long userId) {
        Optional<Streak> existing = streakRepository.findByUserId(userId);
        LocalDate today = LocalDate.now();

        //it means the user is newly added, so there streak must be 1
        if (existing.isEmpty()) {
            Streak streak = new Streak();
            streak.setUserId(userId);
            streak.setCurrentStreak(1);
            streak.setLastLogin(today);
            streakRepository.save(streak);
            return;
        }

        //if streak already exists
        Streak streak = existing.get();
        LocalDate lastLogin = streak.getLastLogin();

        // if user was logged today it should not  change streak count
        if (lastLogin.equals(today)) {
            return;
        }
        // if last login was yesterday it means today this user extended streak
        else if (lastLogin.equals(today.minusDays(1))) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        }
        // if last login was not yesterday it means they have gap so streak must start from 1
        else {
            streak.setCurrentStreak(1);
        }

        streak.setLastLogin(today);
        streakRepository.save(streak);
    }

    // get streak for front
    public Optional<Streak> getStreak(Long userId) {
        return streakRepository.findByUserId(userId);
    }
}