package org.example.y9_gaming_site.streak;

import junit.framework.TestCase;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class StreakControllerTest extends TestCase {

    private StreakService mockStreakService;
    private MockMvc mockMvc;

    @Override
    protected void setUp() throws Exception {
        super.setUp();


        mockStreakService = Mockito.mock(StreakService.class);


        StreakController streakController = new StreakController(mockStreakService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(streakController).build();
    }

    // test1: ამოწმებს, რომ კონტროლერი სწორად აბრუნებს სტრიკის JSON ობიექტს იუზერისთვის
    public void testGetStreak_ReturnsValidStreakJson() throws Exception {
        Long userId = 99L;

        Streak mockStreak = new Streak();
        mockStreak.setId(1L);
        mockStreak.setUserId(userId);
        mockStreak.setCurrentStreak(10);
        mockStreak.setLastLogin(LocalDate.now());

        when(mockStreakService.getStreak(userId)).thenReturn(Optional.of(mockStreak));

        mockMvc.perform(get("/streak/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(99))
                .andExpect(jsonPath("$.currentStreak").value(10));
    }
}