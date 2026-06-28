package org.example.y9_gaming_site.Challenge;


import org.example.y9_gaming_site.friendship.FriendshipRepository;
import org.example.y9_gaming_site.gameRecord.GameRecord;
import org.example.y9_gaming_site.gameRecord.GameRecordService;
import org.example.y9_gaming_site.gameRecord.GameResultEvaluatorRegistry;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameChallengeService {
    private final GameChallengeRepository gameChallengeRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final GameRecordService gameRecordService;
    private final GameResultEvaluatorRegistry gameResultEvaluatorRegistry;

    public GameChallengeService(GameChallengeRepository challengeRepository, UserRepository userRepository,
                                FriendshipRepository friendshipRepository, GameRecordService gameRecordService, GameResultEvaluatorRegistry gameResultEvaluatorRegistry) {
        this.gameChallengeRepository = challengeRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.gameRecordService = gameRecordService;
        this.gameResultEvaluatorRegistry = gameResultEvaluatorRegistry;
    }

    public GameChallenge sendChallenge(Long senderId, Long receiverId, String gameKey, Long contextId) {
        return null; // to do
    }

    public GameChallenge respondToChallenge(Long ChallengeId, Long receiverId, boolean accept) {
        return null; // to do
    }

    public GameChallenge submitAttempt(Long ChallengeId, Long submittingUserId, GameRecord newRecord) {
        return null; // to do
    }

    @Scheduled(cron = "0 0 * * * *")
    public void expireStaleChallenges() { // if user ignores challenge
        return;
    }

    public List<GameChallenge> getInbox(Long userId){
        return null;
    }

    public  List<GameChallenge> getHistory(Long userId){
        return null;
    }
}
