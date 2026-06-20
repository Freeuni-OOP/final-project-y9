package org.example.y9_gaming_site.achievement;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

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

}

