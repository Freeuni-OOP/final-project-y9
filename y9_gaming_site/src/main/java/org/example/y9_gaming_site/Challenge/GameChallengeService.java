package org.example.y9_gaming_site.Challenge;


import org.example.y9_gaming_site.friendship.Friendship;
import org.example.y9_gaming_site.friendship.FriendshipRepository;
import org.example.y9_gaming_site.gameRecord.GameRecord;
import org.example.y9_gaming_site.gameRecord.GameRecordService;
import org.example.y9_gaming_site.gameRecord.GameResultEvaluator;
import org.example.y9_gaming_site.gameRecord.GameResultEvaluatorRegistry;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        if(!theyAreFriends(senderId, receiverId)) {
            throw new RuntimeException("Sender or receiver are not friends");
        }

        GameRecord targetRecord = gameRecordService.findBest(senderId, gameKey, contextId).orElseThrow(()->new RuntimeException("No record found"));
        User receiver = userRepository.findById(receiverId).orElseThrow(()->new RuntimeException("No User found"));
        LocalDateTime expiresAt =  LocalDateTime.now().plusDays(7);
        GameChallenge gameChallenge = new GameChallenge(targetRecord.getUser(), receiver, targetRecord, expiresAt);
        return gameChallengeRepository.save(gameChallenge);

    }

    public GameChallenge respondToChallenge(Long challengeId, Long receiverId, boolean accept) {
        GameChallenge challenge = gameChallengeRepository
                .findById(challengeId).orElseThrow(()->new RuntimeException("No Challenge found"));
        if(!challenge.getReceiver().getId().equals(receiverId)) {
            throw new RuntimeException("only receiver can accept challenge");
        }
        if(challenge.getStatus() != GameChallengeStatus.PENDING) {
            throw new RuntimeException("challenge is not waiting for response");
        }
        if(challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            return expire(challenge);
        }
        if(accept) {
            challenge.setStatus(GameChallengeStatus.ACCEPTED);
        }else {
            challenge.setStatus(GameChallengeStatus.DECLINED);
            challenge.setResolvedAt(LocalDateTime.now());
        }
        return gameChallengeRepository.save(challenge);
    }

    public GameChallenge submitAttempt(Long challengeId, Long submittingUserId, double value, Long contextId) {
        GameChallenge challenge = gameChallengeRepository.findById(challengeId).orElseThrow(()->new RuntimeException("No Challenge found"));
        if(!challenge.getReceiver().getId().equals(submittingUserId)) {
            throw new RuntimeException("only receiver can accept challenge");
        }
        if(challenge.getStatus() != GameChallengeStatus.ACCEPTED) {
            throw new RuntimeException("challenge is not waiting for response");
        }

        if(challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            expire(challenge);
            throw new RuntimeException("challenge has expired");
        }
        GameRecord targRecord = challenge.getTargRecord();
        String gameKey = targRecord.getGame().getTitle();
        GameResultEvaluator eval = gameResultEvaluatorRegistry.resolve(gameKey);

        GameRecord newRecord = gameRecordService.recordResult(submittingUserId,
                gameKey, contextId, value);

        User winner;
        if(eval.isBetter(newRecord.getValue(),  targRecord.getValue())) {
            winner = challenge.getReceiver();
        }else{
            winner = challenge.getSender();
        }
        challenge.setResRecord(newRecord);
        challenge.setWinner(winner);
        challenge.setStatus(GameChallengeStatus.COMPLETED);
        challenge.setResolvedAt(LocalDateTime.now());
        return gameChallengeRepository.save(challenge);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void expireStaleChallenges() { // if user ignores challenge
        List<GameChallenge> overdue = gameChallengeRepository.findByStatusAndExpiresAtBefore(
                GameChallengeStatus.PENDING, LocalDateTime.now());
        for (GameChallenge challenge : overdue) {
            expire(challenge);
        }
    }

    public List<GameChallenge> getInbox(Long userId){
        return gameChallengeRepository.findByReceiverIdAndStatus(userId, GameChallengeStatus.PENDING);
    }

    public  List<GameChallenge> getHistory(Long userId){
        return gameChallengeRepository.findBySenderIdOrReceiverId(userId, userId);
    }

    private GameChallenge expire(GameChallenge challenge) {
        challenge.setStatus(GameChallengeStatus.EXPIRED);
        challenge.setResolvedAt(LocalDateTime.now());
        return gameChallengeRepository.save(challenge);
    }

    private boolean theyAreFriends(Long userId, Long receiverId) {
        Friendship forward = friendshipRepository.findBySenderIdAndReceiverId(userId, receiverId);
        if(forward != null && "ACCEPTED".equalsIgnoreCase(forward.getStatus())) {
            return true;
        }
        Friendship backward = friendshipRepository.findBySenderIdAndReceiverId(receiverId, userId);
        return backward != null && "ACCEPTED".equalsIgnoreCase(backward.getStatus());
    }
}

