package org.example.y9_gaming_site.game.joker;

import org.example.y9_gaming_site.game.joker.JokerGameConfig.PlayerCount;
import org.example.y9_gaming_site.game.joker.JokerGameConfig.RoundOption;
import org.example.y9_gaming_site.game.joker.JokerGameState.GameStatus;
import org.example.y9_gaming_site.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JokerServiceTest {

    private JokerService service;
    private JokerScoringService scService;

    @BeforeEach
    void setUp() {
        scService = Mockito.mock(JokerScoringService.class);
        JokerDbService dbService = Mockito.mock(JokerDbService.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);

        // mock userRepository to return a fake user so createGame doesn't throw
        org.example.y9_gaming_site.user.User fakeUser = Mockito.mock(org.example.y9_gaming_site.user.User.class);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(java.util.Optional.of(fakeUser));

        service = new JokerService(scService, dbService, userRepository);
    }

    @Test
    @DisplayName("Should successfully initialize a game room state map context")
    void testCreateGameLobby() {
        JokerGameState state = service.createGame(10L, "Lasha", PlayerCount.FOUR, RoundOption.FULL_24, 2, true);

        assertNotNull(state);
        assertTrue(state.getRoom().getRoomId().startsWith("JOKER-"));
        assertEquals(1, state.getPlayers().size());
        assertEquals("Lasha", state.getPlayers().get(0).getUsername());
    }


    @Test
    @DisplayName("Should block matching players from joining a full or already occupied active room slot")
    void testJoinGameValidations() {
        JokerGameState state = service.createGame(10L, "Lasha", PlayerCount.FOUR, RoundOption.FULL_24, 2, true);
        String roomId = state.getRoom().getRoomId();

        // Join second player successfully
        service.joinGame(roomId, 11L, "Giorgi");
        assertEquals(2, state.getPlayers().size());

        // Exception test: Joining same game twice
        assertThrows(IllegalStateException.class, () -> service.joinGame(roomId, 11L, "Giorgi"));

        // Exception test: Joining non-existent code reference lookup dictionary key
        assertThrows(IllegalArgumentException.class, () -> service.joinGame("NON_EXIST", 12L, "Nino"));
    }


    @Test
    @DisplayName("Should correctly filter visible active open match room collections")
    void testFindOpenLobbies() {
        JokerGameState state = service.createGame(10L, "Lasha", PlayerCount.FOUR, RoundOption.FULL_24, 2, true);

        List<JokerGameState> openLobbies = service.findOpenLobbies();
        assertFalse(openLobbies.isEmpty());
        assertEquals(state.getRoom().getRoomId(), openLobbies.get(0).getRoom().getRoomId());
    }

    @Test
    @DisplayName("Should validate invite limits across room configurations smoothly")
    void testInvitePlayerRules() {
        JokerGameState state = service.createGame(10L, "Lasha", PlayerCount.FOUR, RoundOption.FULL_24, 2, true);
        String roomId = state.getRoom().getRoomId();

        // Valid execution branch check (does not throw)
        assertDoesNotThrow(() -> service.invitePlayer(roomId, 10L, 99L));

        // Exception test: Unauthorized individual tries issuing invitation parameters
        assertThrows(IllegalStateException.class, () -> service.invitePlayer(roomId, 55L, 99L));
    }

    @Test
    @DisplayName("Should validate host parameters before shifting game into play phases")
    void testStartGameValidations() {
        JokerGameState state = service.createGame(10L, "Lasha", PlayerCount.FOUR, RoundOption.FULL_24, 2, true);
        String roomId = state.getRoom().getRoomId();

        // Exception test: Non-host starts room loop sequence validation tracking
        assertThrows(IllegalStateException.class, () -> service.startGame(roomId, 11L));

        // Exception test: Host tries starting when the lobby count does not match the game variant requirement rules
        assertThrows(IllegalStateException.class, () -> service.startGame(roomId, 10L));
    }

    @Test
    @DisplayName("Should catch illegal parameters and out-of-turn execution requests during bid inputs")
    void testPlaceBidPhaseValidations() {
        JokerGameState state = service.createGame(10L, "Lasha", PlayerCount.FOUR, RoundOption.FULL_24, 2, true);
        String roomId = state.getRoom().getRoomId();

        // Exception test: Bidding out of sequence when game phase state context is WAITING
        assertThrows(IllegalStateException.class, () -> service.placeBid(roomId, 10L, 2));
    }

    @Test
    @DisplayName("Should prevent players from executing play card requests outside active playing round loops")
    void testPlayCardPhaseValidations() {
        JokerGameState state = service.createGame(10L, "Lasha", PlayerCount.FOUR, RoundOption.FULL_24, 2, true);
        String roomId = state.getRoom().getRoomId();

        // Exception test: Phase mismatch rule execution rejection validation
        assertThrows(IllegalStateException.class, () -> service.playCard(roomId, 10L, "HEARTS", 14, "NONE", "NONE"));
    }

    @Test
    @DisplayName("Should deny setting trump suit parameters out of proper turn sequences")
    void testSetTrumpSuitValidation() {
        JokerGameState state = service.createGame(10L, "Lasha", PlayerCount.FOUR, RoundOption.FULL_24, 2, true);
        String roomId = state.getRoom().getRoomId();

        // Pass 99L (not the dealer) to force the IllegalStateException
        assertThrows(IllegalStateException.class, () -> service.setTrumpSuit(roomId, 99L, "SPADES"));
    }
}
