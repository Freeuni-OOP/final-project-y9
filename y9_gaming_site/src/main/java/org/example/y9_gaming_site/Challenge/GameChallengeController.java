package org.example.y9_gaming_site.Challenge;

import org.example.y9_gaming_site.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
public class GameChallengeController {

    private final GameChallengeService gameChallengeService;

    public GameChallengeController(GameChallengeService gameChallengeService) {
        this.gameChallengeService = gameChallengeService;
    }

    private Long currentUserId(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }

    //sender challenges a friend to beat their best score for gameKey contextId
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameChallengeDto send(@RequestBody SendChallengeRequest request, Authentication authentication) {
        Long senderId = currentUserId(authentication);
        GameChallenge challenge = gameChallengeService.sendChallenge(
                senderId, request.getReceiverId(), request.getGameKey(), request.getContextId());
        return GameChallengeDto.from(challenge);
    }

    //receiver accepts or declines
    @PostMapping("/{id}/respond")
    public GameChallengeDto respond(@PathVariable Long id, @RequestBody RespondRequest request, Authentication authentication) {
        Long userId = currentUserId(authentication);
        GameChallenge challenge = gameChallengeService.respondToChallenge(id, userId, request.isAccept());
        return GameChallengeDto.from(challenge);
    }

    //receiver sends score they got while attempting same game
    @PostMapping("/{id}/submit")
    public GameChallengeDto submit(@PathVariable Long id, @RequestBody SubmitAttemptRequest request, Authentication authentication) {
        Long userId = currentUserId(authentication);
        GameChallenge existing = gameChallengeService.findById(id).orElseThrow(() -> new RuntimeException("No Challenge found"));
        Long contextId = existing.getTargRecord().getContextId();
        GameChallenge challenge = gameChallengeService.submitAttempt(id, userId, request.getValue(), contextId);
        return GameChallengeDto.from(challenge);
    }

    //pending challenges waiting on current user to respond
    @GetMapping("/inbox")
    public List<GameChallengeDto> inbox(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return gameChallengeService.getInbox(userId).stream().map(GameChallengeDto::from).toList();
    }

    // every challenge the current user has sent or received
    @GetMapping("/history")
    public List<GameChallengeDto> history(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return gameChallengeService.getHistory(userId).stream().map(GameChallengeDto::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameChallengeDto> getOne(@PathVariable Long id) {
        return gameChallengeService.findById(id)
                .map(c -> ResponseEntity.ok(GameChallengeDto.from(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
