package org.example.y9_gaming_site.game;

import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final UserGameTimeRepository userGameTimeRepository;

    public GameService(GameRepository gameRepository,
                       UserRepository userRepository,
                       UserGameTimeRepository userGameTimeRepository) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.userGameTimeRepository = userGameTimeRepository;
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    @Transactional
    public void trackGameTime(Long gameId, String gameTitle, String category, long durationSeconds, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        UserGameTime trackingLog = new UserGameTime();
        trackingLog.setUser(user);
        trackingLog.setGameTitle(gameTitle);
        trackingLog.setTotalTimeSeconds(durationSeconds);
        trackingLog.setCategory(category != null ? category : "ARCADE");

        userGameTimeRepository.save(trackingLog);
        System.out.println("Saved playtime to database for game: " + gameTitle + " (" + durationSeconds + "s)");
    }
}