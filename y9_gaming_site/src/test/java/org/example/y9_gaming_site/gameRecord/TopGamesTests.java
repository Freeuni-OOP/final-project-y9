package org.example.y9_gaming_site.gameRecord;

import junit.framework.TestCase;
import org.example.y9_gaming_site.game.GameAnalyticsController;
import org.example.y9_gaming_site.game.UserGameTime;
import org.example.y9_gaming_site.game.UserGameTimeRepository;
import org.example.y9_gaming_site.user.Role;
import org.example.y9_gaming_site.user.User;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;

public class TopGamesTests extends TestCase {

    private UserGameTimeRepository mockUserGameTimeRepository;
    private GameAnalyticsController gameAnalyticsController;
    private Authentication mockAuthentication;
    private User regularUser;
    private User guestUser;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mockUserGameTimeRepository = Mockito.mock(UserGameTimeRepository.class);
        gameAnalyticsController = new GameAnalyticsController();

        java.lang.reflect.Field repoField = GameAnalyticsController.class.getDeclaredField("userGameTimeRepository");
        repoField.setAccessible(true);
        repoField.set(gameAnalyticsController, mockUserGameTimeRepository);

        mockAuthentication = Mockito.mock(Authentication.class);

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setUsername("mesata");
        regularUser.setRole(Role.USER);

        guestUser = new User();
        guestUser.setId(3L);
        guestUser.setUsername("guest_user");
        guestUser.setRole(Role.GUEST);
    }

    public void test1() {
        Mockito.when(mockAuthentication.getPrincipal()).thenReturn(regularUser);

        GameAnalyticsController.TimeTrackingRequest request = new GameAnalyticsController.TimeTrackingRequest();
        request.gameTitle = "Swordbattle.io";
        request.category = "ARCADE";
        request.durationSeconds = 120;

        UserGameTime existingRecord = new UserGameTime();
        existingRecord.setId(101L);
        existingRecord.setUser(regularUser);
        existingRecord.setGameTitle("Swordbattle.io");
        existingRecord.setCategory("ARCADE");
        existingRecord.setTotalTimeSeconds(366);

        Mockito.when(mockUserGameTimeRepository.findByUserAndGameTitle(any(User.class), eq("Swordbattle.io")))
                .thenReturn(Optional.of(existingRecord));

        ResponseEntity<?> response = gameAnalyticsController.logTime(mockAuthentication, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(486, existingRecord.getTotalTimeSeconds());
        Mockito.verify(mockUserGameTimeRepository).save(existingRecord);
    }

    public void test2() {
        Mockito.when(mockAuthentication.getPrincipal()).thenReturn(regularUser);

        GameAnalyticsController.TimeTrackingRequest request = new GameAnalyticsController.TimeTrackingRequest();
        request.gameTitle = "New Game";
        request.category = "ACTION";
        request.durationSeconds = 100;

        Mockito.when(mockUserGameTimeRepository.findByUserAndGameTitle(any(User.class), eq("New Game")))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = gameAnalyticsController.logTime(mockAuthentication, request);

        assertEquals(200, response.getStatusCode().value());
        Mockito.verify(mockUserGameTimeRepository).save(any(UserGameTime.class));
    }


    public void test4() {
        Mockito.when(mockAuthentication.getPrincipal()).thenReturn(guestUser);
        ResponseEntity<?> response = gameAnalyticsController.logTime(mockAuthentication, new GameAnalyticsController.TimeTrackingRequest());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Guests are not tracked", ((Map<?, ?>) response.getBody()).get("error"));
    }

    public void test5() {
        UserGameTime g1 = new UserGameTime(); g1.setId(1L); g1.setGameTitle("Stickman Archero Fight"); g1.setCategory("ACTION"); g1.setTotalTimeSeconds(6357);
        UserGameTime g2 = new UserGameTime(); g2.setId(2L); g2.setGameTitle("Helix Jump"); g2.setCategory("ARCADE"); g2.setTotalTimeSeconds(3872);
        UserGameTime g3 = new UserGameTime(); g3.setId(3L); g3.setGameTitle("Duck Life 1"); g3.setCategory("ARCADE"); g3.setTotalTimeSeconds(1035);

        Mockito.when(mockUserGameTimeRepository.findTop3FavoriteGames(eq(2L), any(PageRequest.class)))
                .thenReturn(Arrays.asList(g1, g2, g3));

        ResponseEntity<?> response = gameAnalyticsController.getTop3(2L);

        assertEquals(200, response.getStatusCode().value());
        List<?> body = (List<?>) response.getBody();
        assertNotNull(body);
        assertEquals(3, body.size());
    }

    public void test6() {
        Mockito.when(mockUserGameTimeRepository.findTop3FavoriteGames(eq(2L), any(PageRequest.class)))
                .thenThrow(new RuntimeException("Database offline"));

        ResponseEntity<?> response = gameAnalyticsController.getTop3(2L);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Database offline", ((Map<?, ?>) response.getBody()).get("error"));
    }

    public void test7() {
        Object[] catRow1 = new Object[]{"ACTION", 6357L};
        Object[] catRow2 = new Object[]{"ARCADE", 5755L};
        List<Object[]> mockRows = Arrays.asList(catRow1, catRow2);

        Mockito.when(mockUserGameTimeRepository.findTop3CategoriesByUserId(eq(2L), any(PageRequest.class)))
                .thenReturn(mockRows);

        ResponseEntity<List<GameAnalyticsController.CategoryStatsResponse>> response = gameAnalyticsController.getTopCategories(2L);

        assertEquals(200, response.getStatusCode().value());
        List<GameAnalyticsController.CategoryStatsResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals(2, body.size());
        assertEquals("ACTION", body.get(0).category);
        assertEquals(6357L, body.get(0).totalTimeSeconds);
    }
}