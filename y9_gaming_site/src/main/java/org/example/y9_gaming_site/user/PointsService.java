package org.example.y9_gaming_site.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void spend(Long userId, int amount) {
        int updated = userRepository.spendPoints(userId, amount);
        if (updated == 0) {
            requireUser(userId);
            throw new IllegalStateException("Not enough points");
        }
    }

    @Transactional
    public void credit(Long userId, int amount) {
        int updated = userRepository.addPoints(userId, amount);
        if (updated == 0) {
            requireUser(userId); // throws IllegalArgumentException if the user doesnt exist
        }
    }
}