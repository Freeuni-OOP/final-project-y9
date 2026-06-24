package org.example.y9_gaming_site.admin;

import jakarta.persistence.EntityNotFoundException;
import org.example.y9_gaming_site.game.Game;
import org.example.y9_gaming_site.game.GameRepository;
import org.example.y9_gaming_site.user.Role;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AnnouncementRepository announcementRepository;
    private final ChallengeRepository challengeRepository;
    private final GameRepository gameRepository;

    public AdminService(UserRepository userRepository,
                        AnnouncementRepository announcementRepository,
                        ChallengeRepository challengeRepository,
                        GameRepository gameRepository) {
        this.userRepository = userRepository;
        this.announcementRepository = announcementRepository;
        this.challengeRepository = challengeRepository;
        this.gameRepository = gameRepository;

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


    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAll();
    }

    public void createAnnouncement(AnnouncementDTO dto, String username) {
        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        announcement.setAdmin_id(user.getId());
        announcementRepository.save(announcement);
    }
    public void deleteAnnouncement(Long id) {
        announcementRepository.deleteById(id);
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

    public void deleteGame(Long id){gameRepository.deleteById(id);}
}