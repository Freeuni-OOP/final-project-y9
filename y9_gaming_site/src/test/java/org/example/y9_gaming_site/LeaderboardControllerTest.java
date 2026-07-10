package org.example.y9_gaming_site;

import junit.framework.TestCase;
import org.example.y9_gaming_site.gameRecord.GameRecord;
import org.example.y9_gaming_site.gameRecord.GameRecordService;
import org.example.y9_gaming_site.leaderboard.LeaderboardController;
import org.example.y9_gaming_site.user.User;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

public class LeaderboardControllerTest extends TestCase {

    private GameRecordService mockGameRecordService;
    private MockMvc mockMvc;
    private GameRecord sampleRecord;

    @Override
    protected void setUp() throws Exception {
        super.setUp();


        mockGameRecordService = Mockito.mock(GameRecordService.class);


        LeaderboardController leaderboardController = new LeaderboardController(mockGameRecordService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(leaderboardController).build();


        User mockUser = new User();
        mockUser.setId(10L);
        mockUser.setUsername("ProGamerX");
        mockUser.setAvatarUrl("avatar123.jpg");


        sampleRecord = new GameRecord();
        sampleRecord.setUser(mockUser);
        sampleRecord.setValue(2500.0);
        sampleRecord.setRecordedAt(LocalDateTime.of(2026, 7, 10, 20, 0)); // ფიქსირებული დრო ტესტისთვის
    }

    // test1: ამოწმებს GET /api/leaderboard/{gameName} აბრუნებს თუ არა ტოპ 10-ს (All Time)
    public void testGetTopScoredAllTime() throws Exception {

        Mockito.when(mockGameRecordService.findLeaderboard("TETRIS", null, 10))
                .thenReturn(Arrays.asList(sampleRecord));


        mockMvc.perform(get("/api/leaderboard/tetris")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(10))
                .andExpect(jsonPath("$[0].username").value("ProGamerX"))
                .andExpect(jsonPath("$[0].avatarUrl").value("avatar123.jpg"))
                .andExpect(jsonPath("$[0].score").value(2500.0))
                .andExpect(jsonPath("$[0].playedAt").value(containsString("2026-07-10"))); // ამოწმებს თარიღის ნაწილს JSON-ში
    }

    // test2: ამოწმებს GET /api/leaderboard/{gameName}/today აბრუნებს თუ არა ბოლო 24 საათის მონაცემებს
    public void testGetTopScoresToday() throws Exception {

        Mockito.when(mockGameRecordService.findLeaderboardSince(eq("CHESS"), any(LocalDateTime.class), eq(10)))
                .thenReturn(Arrays.asList(sampleRecord));

        mockMvc.perform(get("/api/leaderboard/chess/today")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("ProGamerX"))
                .andExpect(jsonPath("$[0].score").value(2500.0));
    }
}