package org.example.y9_gaming_site.achievement;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AchievementService {
    private final AchievementRepository achievementRepository;
    private  final UserAchievementRepository userAchievementRepository;

    public AchievementService(AchievementRepository achievementRepository, UserAchievementRepository userAchievementRepository){
        this.achievementRepository=achievementRepository;
        this.userAchievementRepository=userAchievementRepository;
    }

    //returns all achievements this specific user has earned
    public List<UserAchievement> getUserAchievement(Long userId){
        return userAchievementRepository.findByUserId(userId);
    }

    public void grantAchievement(Long userId, long achievementId){
        boolean alreadyEarned=userAchievementRepository.
                findByUserIdAndAchievementId(userId,achievementId).isPresent();
        if(alreadyEarned) return; //already has  this achievement
        UserAchievement currAch=new UserAchievement();
        currAch.setUserId(userId);
        currAch.setAchievementId(achievementId);
        currAch.setEarnedTime(LocalDateTime.now());

        userAchievementRepository.save(currAch);
    }

    public void grantByCode(Long userId, String code){
        if(userId==null || code==null) return;
        achievementRepository.findByCode(code).ifPresent(a -> grantAchievement(userId, a.getId()));
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

}

