package org.example.y9_gaming_site.Quiz;

import junit.framework.TestCase;
import org.example.y9_gaming_site.quiz.Quiz;
import org.example.y9_gaming_site.quiz.QuizService;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;

public class QuizTests extends TestCase {

    private JdbcTemplate mockJdbcTemplate;
    private QuizService quizService;
    private Quiz quiz1;
    private Quiz quiz2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mockJdbcTemplate = Mockito.mock(JdbcTemplate.class);
        quizService = new QuizService(mockJdbcTemplate, null);

        quiz1 = new Quiz();
        quiz1.setId(1L);
        quiz1.setTitle("Capitals of Europe");
        quiz1.setCategory("GEOGRAPHY");
        quiz1.setDescription("Test Desc 1");
        quiz1.setIconUrl("/icon1.png");
        quiz1.setTimeLimitSeconds(120);
        quiz1.setQuestions(new ArrayList<>(List.of("Q1 (A1)")));
        quiz1.setImages(new ArrayList<>(List.of("/img1.png")));

        quiz2 = new Quiz();
        quiz2.setId(2L);
        quiz2.setTitle("Atomic Masses");
        quiz2.setCategory("SCIENCE");
        quiz2.setDescription("Test Desc 2");
        quiz2.setIconUrl("/icon2.png");
        quiz2.setTimeLimitSeconds(180);
        quiz2.setQuestions(new ArrayList<>(List.of("Q2 (A2)")));
        quiz2.setImages(new ArrayList<>(List.of("/img2.png")));
    }

    public void test1() {
        Mockito.when(mockJdbcTemplate.query(eq("SELECT * FROM quizzes"), any(RowMapper.class)))
                .thenReturn(Arrays.asList(quiz1, quiz2));

        List<Quiz> result = quizService.getAllQuizzes();

        assertEquals(2, result.size());
        assertEquals("Capitals of Europe", result.get(0).getTitle());
        assertEquals("GEOGRAPHY", result.get(0).getCategory());
        assertEquals("Test Desc 1", result.get(0).getDescription());
        assertEquals("/icon1.png", result.get(0).getIconUrl());
        assertNull(result.get(0).getCreatedAt());
        assertEquals(120, result.get(0).getTimeLimitSeconds());
        assertEquals(1, result.get(0).getQuestions().size());
        assertEquals(1, result.get(0).getImages().size());
    }

    public void test2() {
        Mockito.when(mockJdbcTemplate.query(eq("SELECT * FROM quizzes"), any(RowMapper.class)))
                .thenReturn(Arrays.asList(quiz1, quiz2));

        List<Quiz> result = quizService.getAllQuizzes();
        List<Quiz> filteredResult = new ArrayList<>();
        for (Quiz q : result) {
            if (q.getCategory().equalsIgnoreCase("GeOgRaPhY")) {
                filteredResult.add(q);
            }
        }

        assertEquals(1, filteredResult.size());
        assertEquals("GEOGRAPHY", filteredResult.get(0).getCategory());
    }

    public void test3() {
        Mockito.when(mockJdbcTemplate.query(eq("SELECT * FROM quizzes"), any(RowMapper.class)))
                .thenReturn(Arrays.asList(quiz1, quiz2));

        List<Quiz> result = quizService.getAllQuizzes();
        List<Quiz> filteredResult = new ArrayList<>();
        for (Quiz q : result) {
            if (q.getCategory().equalsIgnoreCase("ENTERTAINMENT")) {
                filteredResult.add(q);
            }
        }

        assertEquals(0, filteredResult.size());
        assertTrue(filteredResult.isEmpty());
    }

    public void test4() {
        Mockito.when(mockJdbcTemplate.query(eq("SELECT * FROM quizzes WHERE id = ?"), any(RowMapper.class), eq(1L)))
                .thenReturn(Arrays.asList(quiz1));

        Quiz result = quizService.getQuizById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
    }

    public void test5() {
        Mockito.when(mockJdbcTemplate.query(eq("SELECT * FROM quizzes WHERE id = ?"), any(RowMapper.class), eq(99L)))
                .thenReturn(new ArrayList<>());

        try {
            quizService.getQuizById(99L);
            fail("Expected a RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertEquals("Quiz ID 99 not found.", e.getMessage());
        }
    }

    public void test6() {
        List<String> questionTexts = List.of("Is Java cool?");
        List<String> correctAnswers = List.of("Yes");
        List<String> wrongAnswers = List.of("No|Maybe");
        List<String> imagePaths = List.of("/img.png");

        quizService.createQuiz("Java Quiz", "SCIENCE", "Description", 300,
                questionTexts, correctAnswers, wrongAnswers, imagePaths, 42L);

        String expectedSql = "INSERT INTO quizzes (title, category, description, time_limit_seconds, questions_blob, images, creator_id, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

        Mockito.verify(mockJdbcTemplate).update(
                eq(expectedSql),
                eq("Java Quiz"), eq("SCIENCE"), eq("Description"), eq(300),
                eq("Is Java cool? (Yes|No|Maybe)"), eq("[\"/img.png\"]"), eq(42L)
        );
    }

    public void test7() {
        List<String> questionTexts = List.of("Q1", "Q2", "Q3");
        List<String> correctAnswers = List.of("A1", "A2", "A3");
        List<String> wrongAnswers = Arrays.asList(null, "", "NO_WRONG_ANSWERS");
        List<String> imagePaths = Arrays.asList(null, "", "/path.png");

        quizService.createQuiz("Geo", "GEOGRAPHY", "Desc", 60,
                questionTexts, correctAnswers, wrongAnswers, imagePaths, 42L);

        Mockito.verify(mockJdbcTemplate).update(
                any(String.class),
                eq("Geo"), eq("GEOGRAPHY"), eq("Desc"), eq(60),
                eq("Q1 (A1);Q2 (A2);Q3 (A3)"), eq("[\"\",\"\",\"/path.png\"]"), eq(42L)
        );
    }

    public void test8() {
        quizService.deleteQuiz(10L);
        Mockito.verify(mockJdbcTemplate).update(eq("DELETE FROM quizzes WHERE id = ?"), eq(10L));
    }

    public void test9() throws Exception {
        ResultSet mockRs = Mockito.mock(ResultSet.class);
        Mockito.when(mockRs.getLong("id")).thenReturn(7L);
        Mockito.when(mockRs.getString("title")).thenReturn("Empty Fields Quiz");
        Mockito.when(mockRs.getString("category")).thenReturn("SCIENCE");
        Mockito.when(mockRs.getString("description")).thenReturn("Testing null blobs");
        Mockito.when(mockRs.getInt("time_limit_seconds")).thenReturn(100);

        Mockito.when(mockRs.getString("questions_blob")).thenReturn(null);
        Mockito.when(mockRs.getString("images")).thenReturn("   ");

        org.mockito.ArgumentCaptor<RowMapper> mapperCaptor = org.mockito.ArgumentCaptor.forClass(RowMapper.class);
        quizService.getAllQuizzes();

        Mockito.verify(mockJdbcTemplate).query(anyString(), mapperCaptor.capture());
        RowMapper<Quiz> capturedMapper = mapperCaptor.getValue();

        Quiz resultQuiz = capturedMapper.mapRow(mockRs, 1);

        assertNotNull(resultQuiz);
        assertEquals(7L, resultQuiz.getId().longValue());
        assertEquals(0, resultQuiz.getQuestions().size());
        assertEquals(0, resultQuiz.getImages().size());
    }

    public void test10() throws Exception {
        ResultSet mockRs = Mockito.mock(ResultSet.class);
        Mockito.when(mockRs.getLong("id")).thenReturn(8L);
        Mockito.when(mockRs.getString("title")).thenReturn("Malform JSON Quiz");
        Mockito.when(mockRs.getString("category")).thenReturn("GEOGRAPHY");

        Mockito.when(mockRs.getString("questions_blob")).thenReturn("Q1 (A1);Q2 (A2)");
        Mockito.when(mockRs.getString("images")).thenReturn("{broken-json-brackets: true}");

        org.mockito.ArgumentCaptor<RowMapper> mapperCaptor = org.mockito.ArgumentCaptor.forClass(RowMapper.class);
        quizService.getAllQuizzes();

        Mockito.verify(mockJdbcTemplate).query(anyString(), mapperCaptor.capture());
        RowMapper<Quiz> capturedMapper = mapperCaptor.getValue();

        Quiz resultQuiz = capturedMapper.mapRow(mockRs, 1);

        assertNotNull(resultQuiz);
        assertEquals(2, resultQuiz.getQuestions().size());
        assertEquals(0, resultQuiz.getImages().size());
    }
}