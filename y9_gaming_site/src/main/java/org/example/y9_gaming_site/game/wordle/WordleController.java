package org.example.y9_gaming_site.game.wordle;

import org.example.y9_gaming_site.game.wordle.dto.AttemptStateDto;
import org.example.y9_gaming_site.game.wordle.dto.GuessRequest;
import org.example.y9_gaming_site.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games/wordle")
public class WordleController {

    private WordleService wordleService;
    public WordleController(WordleService wordleService) {
        this.wordleService = wordleService;
    }

    @GetMapping("/daily")
    public AttemptStateDto getDaily(Authentication authentication) {
        // just daily puzzle
        Long userId = ((User) authentication.getPrincipal()).getId();
        WordlePuzzle puzzle = wordleService.getOrCreateDailyPuzzle();
        return wordleService.getAttemptState(userId, puzzle.getId());
    }

    @PostMapping("/practice")
    @ResponseStatus(HttpStatus.CREATED)
    public AttemptStateDto startPractice(Authentication authentication) {
        //practice puzzle
        Long userId = ((User) authentication.getPrincipal()).getId();
        WordlePuzzle puzzle = wordleService.practicePuzzle();
        return wordleService.getAttemptState(userId, puzzle.getId());
    }

    @GetMapping("/{puzzleId}")
    public AttemptStateDto getAttemptState(@PathVariable("puzzleId") Long puzzleId, Authentication authentication) {
        //for challenge to give same wordle word
        Long userId = ((User) authentication.getPrincipal()).getId();
        return wordleService.getAttemptState(userId, puzzleId);
    }

    @PostMapping("/{puzzleId}/guess")
    public AttemptStateDto guess(@PathVariable("puzzleId") Long puzzleId,
                                 @RequestBody GuessRequest request, Authentication authentication) {
        Long userId = ((User) authentication.getPrincipal()).getId();
        return wordleService.submitGuess(userId, puzzleId, request.guess());
    }

    @PostMapping("/{puzzleId}/hint")
    public AttemptStateDto hint(@PathVariable("puzzleId") Long puzzleId, Authentication authentication) {
        Long userId = ((User) authentication.getPrincipal()).getId();
        return wordleService.useHint(userId, puzzleId);
    }
}
