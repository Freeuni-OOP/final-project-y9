package org.example.y9_gaming_site.user;

import org.springframework.stereotype.Service;

@Service
public class PointsService {

    private final UserRepository userRepository;

    public PointsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User id " + userId + " not found!"));
    }

    public int getBalance(Long userId) {
        return requireUser(userId).getPoints();
    }

    public boolean hasEnough(Long userId, int amount) {
        return getBalance(userId) >= amount;
    }

    public void spend(Long userId, int amount) {
        User user = requireUser(userId);
        if (user.getPoints() < amount) {
            throw new IllegalStateException("Not enough points");
        }
        user.setPoints(user.getPoints() - amount);
        userRepository.save(user);
    }

    public void credit(Long userId, int amount) {
        User user = requireUser(userId);
        user.setPoints(user.getPoints() + amount);
        userRepository.save(user);
    }
}
