package org.example.y9_gaming_site.game.joker;

import lombok.RequiredArgsConstructor;
import org.example.y9_gaming_site.game.joker.JokerGameConfig.PlayerCount;
import org.example.y9_gaming_site.game.joker.JokerGameConfig.RoundOption;
import org.example.y9_gaming_site.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/joker")
@RequiredArgsConstructor
public class JokerController {

    private final JokerService jokerService;
    private final JokerWebSocketService jokerWebSocketService;

    // --- DTOs ---

    public record CreateGameRequest(
            PlayerCount playerCount,
            RoundOption roundOption,
            int jokerAmount,
            boolean allowRandoms
    ) {}

    public record BidRequest(int bid) {}

    public record PlayCardRequest(
            String suit,
            Integer value,
            String jokerCall,
            String declaredSuit
    ) {}

    public record SetTrumpRequest(String suit) {}

    public record InviteRequest(Long receiverId) {}

    // --- Endpoints ---

    @PostMapping("/create")
    public ResponseEntity<JokerGameState> createGame(
            Authentication authentication,
            @RequestBody CreateGameRequest req
    ) {
        User user = (User) authentication.getPrincipal();
        JokerGameState state = jokerService.createGame(
                user.getId(), user.getUsername(),
                req.playerCount(), req.roundOption(),
                req.jokerAmount(), req.allowRandoms()
        );
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{roomCode}/join")
    public ResponseEntity<JokerGameState> joinGame(
            Authentication authentication,
            @PathVariable String roomCode
    ) {
        User user = (User) authentication.getPrincipal();
        JokerGameState state = jokerService.joinGame(roomCode, user.getId(), user.getUsername());

        // გადავცემთ state-ს, რომ ვებ-სოკეტმა ყველას დაურენდეროს ახალი მოთამაშე
        jokerWebSocketService.broadcastPlayerJoined(roomCode, state);

        return ResponseEntity.ok(state);
    }

    @GetMapping("/lobbies")
    public ResponseEntity<List<JokerGameState>> getOpenLobbies() {
        return ResponseEntity.ok(jokerService.findOpenLobbies());
    }

    @PostMapping("/{roomCode}/invite")
    public ResponseEntity<Void> invitePlayer(
            Authentication authentication,
            @PathVariable String roomCode,
            @RequestBody InviteRequest req
    ) {
        User user = (User) authentication.getPrincipal();
        jokerService.invitePlayer(roomCode, user.getId(), req.receiverId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomCode}/start")
    public ResponseEntity<Void> startGame(
            Authentication authentication,
            @PathVariable String roomCode
    ) {
        User user = (User) authentication.getPrincipal();
        jokerService.startGame(roomCode, user.getId());


        JokerGameState state = jokerService.getGameState(roomCode);
        jokerWebSocketService.broadcastGameStarted(roomCode, state);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomCode}/bid")
    public ResponseEntity<Void> placeBid(
            Authentication authentication,
            @PathVariable String roomCode,
            @RequestBody BidRequest req
    ) {
        User user = (User) authentication.getPrincipal();
        jokerService.placeBid(roomCode, user.getId(), req.bid());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomCode}/play")
    public ResponseEntity<JokerTrick> playCard(
            Authentication authentication,
            @PathVariable String roomCode,
            @RequestBody PlayCardRequest req
    ) {
        User user = (User) authentication.getPrincipal();
        String call = (req.jokerCall() == null) ? "NONE" : req.jokerCall().toUpperCase();
        String declared = (req.declaredSuit() == null) ? "NONE" : req.declaredSuit().toUpperCase();
        String suit = (req.suit() == null) ? "NONE" : req.suit().toUpperCase();

        JokerTrick trick = jokerService.playCard(
                roomCode, user.getId(),
                suit, req.value(),
                call, declared
        );
        return ResponseEntity.ok(trick);
    }

    @PostMapping("/{roomCode}/trump")
    public ResponseEntity<Void> setTrumpSuit(
            Authentication authentication,
            @PathVariable String roomCode,
            @RequestBody SetTrumpRequest req
    ) {
        User user = (User) authentication.getPrincipal();
        String suit = (req.suit() == null) ? "NONE" : req.suit().toUpperCase();
        jokerService.setTrumpSuit(roomCode, user.getId(), suit);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roomCode}/state")
    public ResponseEntity<JokerGameState> getState(
            @PathVariable String roomCode
    ) {
        return ResponseEntity.ok(jokerService.getGameState(roomCode));
    }
}