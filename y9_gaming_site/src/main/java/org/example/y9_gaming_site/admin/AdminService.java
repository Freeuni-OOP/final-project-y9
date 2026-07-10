package org.example.y9_gaming_site.admin;

import jakarta.persistence.EntityNotFoundException;
import org.example.y9_gaming_site.game.Game;
import org.example.y9_gaming_site.game.GameRepository;
import org.example.y9_gaming_site.user.Role;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final GameRepository gameRepository;
    private final JdbcTemplate jdbcTemplate;

    public AdminService(UserRepository userRepository,
                        ChallengeRepository challengeRepository,
                        GameRepository gameRepository, JdbcTemplate jdbcTemplate) {

        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.gameRepository = gameRepository;

        this.jdbcTemplate = jdbcTemplate;
    }


    public List<User> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .filter(a -> !a.getBanned())
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void changeUserRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    public void banUser(Long id, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBanned(true);
        userRepository.save(user);
    }

    public List<User> getAllBannedUsers() {
        return userRepository.findAll().stream()
                .filter(User::getBanned)
                .toList();
    }


    public void unbanUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banned user not found"));
        user.setBanned(false);
        userRepository.save(user);

    }





    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    public void createChallenge(ChallengeDTO dto, String username) throws AccessDeniedException {
        Challenge challenge = new Challenge();
        challenge.setTitle(dto.getTitle());
        challenge.setDescription(dto.getDescription());
        challenge.setReward(dto.getReward());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));


        challenge.setAdmin_id(user.getId());
        challengeRepository.save(challenge);
    }

    public void deleteChallenge(Long id) {
        challengeRepository.deleteById(id);
    }

    public List<Game> getAllGames(){return gameRepository.findAll();}

//    public void deleteGame(Long id){gameRepository.deleteById(id);}
//
//    //custom quiz making.
//    public void saveCustomQuiz(String title, String category, int timeLimit, String description, String rawQuestions) {
//        String formattedQuestions = rawQuestions.replace("\r\n", ";").replace("\n", ";");
//        String sql = "INSERT INTO quizzes (title, category, time_limit_seconds, description, questions_blob, created_at) " +
//                "VALUES (?, ?, ?, ?, ?, NOW())";
//        jdbcTemplate.update(sql, title, category.toUpperCase(), timeLimit, description, formattedQuestions);
//    }
}