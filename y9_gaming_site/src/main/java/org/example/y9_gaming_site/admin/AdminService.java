package org.example.y9_gaming_site.admin;

import org.example.y9_gaming_site.user.Role;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AnnouncementRepository announcementRepository;
    private final ChallengeRepository challengeRepository;

    public AdminService(UserRepository userRepository,
                        AnnouncementRepository announcementRepository,
                        ChallengeRepository challengeRepository) {
        this.userRepository = userRepository;
        this.announcementRepository = announcementRepository;
        this.challengeRepository = challengeRepository;

    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
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


    public void unbanUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banned user not found"));
        user.setBanned(false);
        userRepository.save(user);

    }


    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAll();
    }

    public void createAnnouncement(AnnouncementDTO dto) {
        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcementRepository.save(announcement);
    }

    public void deleteAnnouncement(Long id) {
        announcementRepository.deleteById(id);
    }



    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    public void createChallenge(ChallengeDTO dto) {
        Challenge challenge = new Challenge();
        challenge.setTitle(dto.getTitle());
        challenge.setDescription(dto.getDescription());
        challenge.setReward(dto.getReward());
        challengeRepository.save(challenge);
    }

    public void deleteChallenge(Long id) {
        challengeRepository.deleteById(id);
    }
}