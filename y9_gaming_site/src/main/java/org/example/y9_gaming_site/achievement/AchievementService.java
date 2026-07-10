package org.example.y9_gaming_site.achievement;
import org.springframework.transaction.annotation.Transactional;

import org.example.y9_gaming_site.user.Role;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AchievementService {
    private final AchievementRepository achievementRepository;
    private  final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;

    public AchievementService(AchievementRepository achievementRepository, UserAchievementRepository userAchievementRepository, UserRepository userRepository){
        this.achievementRepository=achievementRepository;
        this.userAchievementRepository=userAchievementRepository;
        this.userRepository=userRepository;
    }

    //returns all achievements this specific user has earned
    public List<UserAchievement> getUserAchievement(Long userId){
        return userAchievementRepository.findByUserId(userId);
    }

//    public Optional<Achievement> grantAchievement(Long userId, long achievementId){
//        if(isGuest(userId)) return Optional.empty();
//
//        boolean alreadyEarned=userAchievementRepository.
//                findByUserIdAndAchievementId(userId,achievementId).isPresent();
//        if(alreadyEarned) return Optional.empty();
//        UserAchievement currAch=new UserAchievement();
//        currAch.setUserId(userId);
//        currAch.setAchievementId(achievementId);
//        currAch.setEarnedTime(LocalDateTime.now());
//
//        userAchievementRepository.save(currAch);
//        return achievementRepository.findById(achievementId);
//    }

    private boolean isGuest(Long userId){
        return userRepository.findById(userId)
                .map(u -> u.getRole() == Role.GUEST)
                .orElse(false);
    }
    @Transactional
    public Optional<Achievement> grantByCode(Long userId, String code){
        if(userId==null || code==null) return Optional.empty();
        return achievementRepository.findByCode(code).flatMap(a -> grantAchievement(userId, a.getId()));
    }

    public List<AchievementView> getEarnedView(Long userId){
        List<UserAchievement> earned=userAchievementRepository.findByUserId(userId);
        if(earned.isEmpty()) return List.of();
        Map<Long, Achievement> catalog = achievementRepository.findAll().stream().collect(Collectors.toMap(
                Achievement::getId,
                achievement->achievement
        ));
        List<AchievementView> out = new ArrayList<>();
        for(UserAchievement each:earned){
            Achievement a = catalog.get(each.getAchievementId());
            if(a==null) continue;

            long earnedCount = userAchievementRepository.countByAchievementId(a.getId());
            out.add(new AchievementView(a.getCode(), a.getName(), a.getDescription()
                    , each.getEarnedTime(), earnedCount));
        }
        return out;
    }

    public List<AchievementView> getRarestEarned(Long userId, int limit){
        return getEarnedView(userId).stream().sorted(Comparator.comparingLong(
                AchievementView::earnedCount).thenComparing(AchievementView::earnedTime,
                Comparator.nullsLast(Comparator.reverseOrder()))).limit(Math.max(0, limit)).toList();
    }

    @Transactional
    public Optional<Achievement> grantAchievement(Long userId, long achievementId){
        if(isGuest(userId)) return Optional.empty();

        boolean alreadyEarned = userAchievementRepository
                .findByUserIdAndAchievementId(userId, achievementId).isPresent();
        if(alreadyEarned) return Optional.empty();

        UserAchievement currAch = new UserAchievement();
        currAch.setUserId(userId);
        currAch.setAchievementId(achievementId);
        currAch.setEarnedTime(LocalDateTime.now());
        userAchievementRepository.save(currAch);

        Optional<Achievement> achievement = achievementRepository.findById(achievementId);
        achievement.ifPresent(a -> userRepository.addPoints(userId, a.getPointReward()));

        return achievement;
    }
}

