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
    private final BannedUserRepository bannedUserRepository;

    public AdminService(UserRepository userRepository,
                        AnnouncementRepository announcementRepository,
                        ChallengeRepository challengeRepository,
                        BannedUserRepository bannedUserRepository) {
        this.userRepository = userRepository;
        this.announcementRepository = announcementRepository;
        this.challengeRepository = challengeRepository;
        this.bannedUserRepository = bannedUserRepository;

    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<BannedUser> getAllBannedUsers() {
        return bannedUserRepository.findAll();
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
        BannedUser bannedUser = new BannedUser();
        bannedUser.setReason(reason);
        bannedUser.setAge(user.getAge());
        bannedUser.setId(user.getId());
        bannedUser.setEmail(user.getEmail());
        bannedUser.setRole(user.getRole());
        bannedUser.setPassword(user.getPassword());
        bannedUser.setUsername(user.getUsername());
        bannedUser.setSalt(user.getSalt());
        bannedUserRepository.save(bannedUser);
        userRepository.deleteById(id);
    }


    public void unbanUser(Long id) {
        BannedUser bannedUser = bannedUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banned user not found"));
        User user = new User();
        user.setAge(bannedUser.getAge());
        user.setId(bannedUser.getId());
        user.setEmail(bannedUser.getEmail());
        user.setRole(bannedUser.getRole());
        user.setPassword(bannedUser.getPassword());
        user.setUsername(bannedUser.getUsername());
        user.setSalt(bannedUser.getSalt());
        bannedUserRepository.delete(bannedUser);
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