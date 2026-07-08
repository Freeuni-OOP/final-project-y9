package org.example.y9_gaming_site;

import junit.framework.TestCase;
import org.example.y9_gaming_site.quiz.Quiz;
import org.example.y9_gaming_site.quiz.QuizController;
import org.example.y9_gaming_site.quiz.QuizService;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class QuizControllerTest extends TestCase {

    private QuizService mockQuizService;
    private MockMvc mockMvc;
    private Quiz sampleQuiz;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // 1. Mock the backend service layer dependencies
        mockQuizService = Mockito.mock(QuizService.class);

        // 2. Inject into the controller and initialize standalone MockMvc context
        QuizController quizController = new QuizController(mockQuizService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(quizController).build();

        // 3. Create mock domain objects
        sampleQuiz = new Quiz();
        sampleQuiz.setId(5L);
        sampleQuiz.setTitle("Gaming Trivia");
        sampleQuiz.setCategory("ENTERTAINMENT");
    }

    // test1: Verify GET /api/quizzes returns a valid JSON array status
    public void test1() throws Exception {
        Mockito.when(mockQuizService.getAllQuizzes()).thenReturn(Arrays.asList(sampleQuiz));

        mockMvc.perform(get("/api/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].title").value("Gaming Trivia"))
                .andExpect(jsonPath("$[0].category").value("ENTERTAINMENT"));
    }

    // test2: Verify GET /api/quizzes/category/{category} routes filtering requests accurately
    public void test2() throws Exception {
        Mockito.when(mockQuizService.getQuizzesByCategory("ENTERTAINMENT"))
                .thenReturn(Arrays.asList(sampleQuiz));

        mockMvc.perform(get("/api/quizzes/category/ENTERTAINMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Gaming Trivia"));
    }

    // test3: Verify GET /api/quizzes/{id} fetches target individual entities
    public void test3() throws Exception {
        Mockito.when(mockQuizService.getQuizById(5L)).thenReturn(sampleQuiz);

        mockMvc.perform(get("/api/quizzes/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Gaming Trivia"));
    }

    // test4: Verify DELETE /api/quizzes/{id} cleanly logs a success message object
    public void test4() throws Exception {
        Mockito.doNothing().when(mockQuizService).deleteQuiz(5L);

        mockMvc.perform(delete("/api/quizzes/5")
                        .header("Authorization", "Bearer mock-admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Quiz deleted successfully"));

        Mockito.verify(mockQuizService, Mockito.times(1)).deleteQuiz(5L);
    }

    // test5: Verify DELETE /api/quizzes/{id} failure states drop back down to an internal server error status code
    public void test5() throws Exception {
        Mockito.doThrow(new RuntimeException("SQL Failure Exception Context"))
                .when(mockQuizService).deleteQuiz(5L);

        mockMvc.perform(delete("/api/quizzes/5")
                        .header("Authorization", "Bearer mock-admin-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to delete quiz"));
    }

    // test6: Verify POST /api/quizzes/new unloads incoming MultiPart format items and properly redirects users to /home
    public void test6() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "questionImage",
                "avatar.jpg",
                "image/jpeg",
                "raw-bytes-payload".getBytes()
        );

        mockMvc.perform(multipart("/api/quizzes/new")
                        .file(mockFile)
                        .param("title", "Geography Blitz")
                        .param("category", "GEOGRAPHY")
                        .param("description", "A fast paced challenge")
                        .param("timeLimit", "300")
                        .param("questionText", "Where is Tbilisi?")
                        .param("correctAnswer", "Georgia")
                        .param("wrongAnswers", "France|Spain|Japan"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(flash().attribute("message", "Quiz published!"));

        // Verifies the controller successfully extracted parameters down to service invocation layer
        Mockito.verify(mockQuizService, Mockito.times(1)).createQuiz(
                eq("Geography Blitz"), eq("GEOGRAPHY"), eq("A fast paced challenge"), eq(300),
                anyList(), anyList(), anyList(), anyList()
        );
    }
}