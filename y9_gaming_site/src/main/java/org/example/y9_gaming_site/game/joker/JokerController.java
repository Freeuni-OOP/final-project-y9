package org.example.y9_gaming_site.game.joker;

import lombok.RequiredArgsConstructor;
import org.example.y9_gaming_site.game.joker.JokerGameConfig.PlayerCount;
import org.example.y9_gaming_site.game.joker.JokerGameConfig.RoundOption;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/joker")
@RequiredArgsConstructor
public class JokerController {

    private final JokerService jokerService;

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
            String jokerCall,   // FIXED: Changed from custom enum to String ("HIGH", "LOW", or "NONE")
            String declaredSuit // "HEARTS", "DIAMONDS", "CLUBS", "SPADES", or "NONE"
    ) {}

    public record SetTrumpRequest(String suit) {}

    public record InviteRequest(Long receiverId) {}

    // --- Endpoints ---

    // POST /api/joker/create
    @PostMapping("/create")
    public ResponseEntity<JokerGameState> createGame(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateGameRequest req
    ) {
        Long userId = extractUserId(userDetails);
        String username = userDetails.getUsername();
        JokerGameState state = jokerService.createGame(
                userId, username,
                req.playerCount(), req.roundOption(),
                req.jokerAmount(), req.allowRandoms()
        );
        return ResponseEntity.ok(state);
    }

    // POST /api/joker/{roomCode}/join
    @PostMapping("/{roomCode}/join")
    public ResponseEntity<JokerGameState> joinGame(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String roomCode
    ) {
        Long userId = extractUserId(userDetails);
        String username = userDetails.getUsername();
        JokerGameState state = jokerService.joinGame(roomCode, userId, username);
        return ResponseEntity.ok(state);
    }

    // GET /api/joker/lobbies
    @GetMapping("/lobbies")
    public ResponseEntity<List<JokerGameState>> getOpenLobbies() {
        return ResponseEntity.ok(jokerService.findOpenLobbies());
    }

    // POST /api/joker/{roomCode}/invite
    @PostMapping("/{roomCode}/invite")
    public ResponseEntity<Void> invitePlayer(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String roomCode,
            @RequestBody InviteRequest req
    ) {
        Long inviterId = extractUserId(userDetails);
        jokerService.invitePlayer(roomCode, inviterId, req.receiverId());
        return ResponseEntity.ok().build();
    }

    // POST /api/joker/{roomCode}/start
    @PostMapping("/{roomCode}/start")
    public ResponseEntity<Void> startGame(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String roomCode
    ) {
        Long hostId = extractUserId(userDetails);
        jokerService.startGame(roomCode, hostId);
        return ResponseEntity.ok().build();
    }

    // POST /api/joker/{roomCode}/bid
    @PostMapping("/{roomCode}/bid")
    public ResponseEntity<Void> placeBid(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String roomCode,
            @RequestBody BidRequest req
    ) {
        Long userId = extractUserId(userDetails);
        jokerService.placeBid(roomCode, userId, req.bid());
        return ResponseEntity.ok().build();
    }

    // POST /api/joker/{roomCode}/play
    @PostMapping("/{roomCode}/play")
    public ResponseEntity<JokerTrick> playCard(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String roomCode,
            @RequestBody PlayCardRequest req
    ) {
        Long userId = extractUserId(userDetails);

        // FIXED: Normalize null values from client json mappings safely to "NONE"
        String call = (req.jokerCall() == null) ? "NONE" : req.jokerCall().toUpperCase();
        String declared = (req.declaredSuit() == null) ? "NONE" : req.declaredSuit().toUpperCase();
        String targetSuit = (req.suit() == null) ? "NONE" : req.suit().toUpperCase();

        JokerTrick trick = jokerService.playCard(
                roomCode, userId,
                targetSuit, req.value(),
                call, declared
        );
        return ResponseEntity.ok(trick);
    }

    // POST /api/joker/{roomCode}/trump
    @PostMapping("/{roomCode}/trump")
    public ResponseEntity<Void> setTrumpSuit(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String roomCode,
            @RequestBody SetTrumpRequest req
    ) {
        Long userId = extractUserId(userDetails);
        String targetSuit = (req.suit() == null) ? "NONE" : req.suit().toUpperCase();
        jokerService.setTrumpSuit(roomCode, userId, targetSuit);
        return ResponseEntity.ok().build();
    }

    // GET /api/joker/{roomCode}/state
    @GetMapping("/{roomCode}/state")
    public ResponseEntity<JokerGameState> getState(
            @PathVariable String roomCode
    ) {
        return ResponseEntity.ok(jokerService.getGameState(roomCode));
    }

    // --- Helper ---

    private Long extractUserId(UserDetails userDetails) {
        // Keeps user evaluation decoupled clean
        if (userDetails instanceof org.example.y9_gaming_site.user.User user) {
            return user.getId();
        }
        // Fallback or custom extraction code if security definitions shift
        throw new IllegalStateException("Cannot extract userId from UserDetails context definition");
    }
}