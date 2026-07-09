//package org.example.y9_gaming_site;
//
//import junit.framework.TestCase;
//import org.example.y9_gaming_site.game.GameAnalyticsController;
//import org.example.y9_gaming_site.game.UserGameTime;
//import org.example.y9_gaming_site.game.UserGameTimeRepository;
//import org.example.y9_gaming_site.user.User;
//import org.example.y9_gaming_site.user.UserRepository;
//import org.mockito.Mockito;
//import org.springframework.http.ResponseEntity;
//
//import java.security.Principal;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.*;
//
//public class TopGamesTests extends TestCase {
//
//    private UserGameTimeRepository mockUserGameTimeRepository;
//    private UserRepository mockUserRepository;
//    private GameAnalyticsController gameAnalyticsController;
//
//    private User user2;
//    private Principal principalUser2;
//
//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//
//        mockUserGameTimeRepository = Mockito.mock(UserGameTimeRepository.class);
//        mockUserRepository = Mockito.mock(UserRepository.class);
//        gameAnalyticsController = new GameAnalyticsController();
//
//        java.lang.reflect.Field repoField = GameAnalyticsController.class.getDeclaredField("userGameTimeRepository");
//        repoField.setAccessible(true);
//        repoField.set(gameAnalyticsController, mockUserGameTimeRepository);
//
//        java.lang.reflect.Field userRepoField = GameAnalyticsController.class.getDeclaredField("userRepository");
//        userRepoField.setAccessible(true);
//        userRepoField.set(gameAnalyticsController, mockUserRepository);
//
//        user2 = new User();
//        user2.setId(2L);
//        user2.setUsername("mesata");
//
//        principalUser2 = Mockito.mock(Principal.class);
//        Mockito.when(principalUser2.getName()).thenReturn("mesata");
//    }
//
//    public void test1() {
//        Mockito.when(mockUserRepository.findByUsername("mesata")).thenReturn(Optional.of(user2));
//
//        GameAnalyticsController.TimeTrackingRequest request = new GameAnalyticsController.TimeTrackingRequest();
//        request.gameTitle = "Swordbattle.io";
//        request.category = "ARCADE";
//        request.durationSeconds = 120;
//
//        UserGameTime existingRecord = new UserGameTime();
//        existingRecord.setId(101L);
//        existingRecord.setUser(user2);
//        existingRecord.setGameTitle("Swordbattle.io");
//        existingRecord.setTotalTimeSeconds(366);
//
//        Mockito.when(mockUserGameTimeRepository.findByUserAndGameTitle(any(User.class), eq("Swordbattle.io")))
//                .thenReturn(Optional.of(existingRecord));
//
//        ResponseEntity<?> response = gameAnalyticsController.logTime(principalUser2, request);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertEquals(486, existingRecord.getTotalTimeSeconds());
//    }
//
//    public void test2() {
//        Mockito.when(mockUserRepository.findByUsername("mesata")).thenReturn(Optional.of(user2));
//
//        UserGameTime g1 = new UserGameTime(); g1.setGameTitle("Stickman Archero Fight"); g1.setTotalTimeSeconds(6357);
//        UserGameTime g2 = new UserGameTime(); g2.setGameTitle("Helix Jump"); g2.setTotalTimeSeconds(3872);
//        UserGameTime g3 = new UserGameTime(); g3.setGameTitle("Duck Life 1"); g3.setTotalTimeSeconds(1035);
//
//        Mockito.when(mockUserGameTimeRepository.findTop3FavoriteGames(2L)).thenReturn(Arrays.asList(g1, g2, g3));
//
//        ResponseEntity<List<UserGameTime>> response = gameAnalyticsController.getMyTop3(principalUser2);
//
//        assertEquals(200, response.getStatusCode().value());
//        List<UserGameTime> body = response.getBody();
//        assertNotNull(body);
//        assertEquals(3, body.size());
//        assertEquals("Stickman Archero Fight", body.get(0).getGameTitle());
//        assertEquals("Helix Jump", body.get(1).getGameTitle());
//        assertEquals("Duck Life 1", body.get(2).getGameTitle());
//    }
//
//    public void test3() {
//        Mockito.when(mockUserRepository.findByUsername("mesata")).thenReturn(Optional.of(user2));
//
//        Object[] catRow1 = new Object[]{"ACTION", 6357L};
//        Object[] catRow2 = new Object[]{"ARCADE", 5755L};
//        List<Object[]> mockRows = Arrays.asList(catRow1, catRow2);
//
//        Mockito.when(mockUserGameTimeRepository.findTop5CategoriesByUserId(2L)).thenReturn(mockRows);
//
//        ResponseEntity<List<GameAnalyticsController.CategoryStatsResponse>> response = gameAnalyticsController.getMyTopCategories(principalUser2);
//
//        assertEquals(200, response.getStatusCode().value());
//        List<GameAnalyticsController.CategoryStatsResponse> body = response.getBody();
//        assertNotNull(body);
//        assertEquals(2, body.size());
//        assertEquals("ACTION", body.get(0).category);
//        assertEquals(6357L, body.get(0).totalTimeSeconds);
//    }
//}
