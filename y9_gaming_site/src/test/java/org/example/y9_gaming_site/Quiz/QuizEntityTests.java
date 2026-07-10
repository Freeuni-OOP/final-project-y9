package org.example.y9_gaming_site.Quiz;

import junit.framework.TestCase;
import org.example.y9_gaming_site.quiz.Quiz;

import java.util.ArrayList;
import java.util.List;

public class QuizEntityTests extends TestCase {

    public void test1() {
        Quiz quiz = new Quiz();

        quiz.setId(10L);
        quiz.setTitle("History Quiz");
        quiz.setDescription("Test Description");
        quiz.setCategory("HISTORY");
        quiz.setIconUrl("/icons/history.png");
        quiz.setTimeLimitSeconds(150);

        List<String> questionsList = new ArrayList<>(List.of("Q1", "Q2"));
        quiz.setQuestions(questionsList);

        List<String> imagesList = new ArrayList<>(List.of("/img1.png", "/img2.png"));
        quiz.setImages(imagesList);

        assertEquals(10L, quiz.getId().longValue());
        assertEquals("History Quiz", quiz.getTitle());
        assertEquals("Test Description", quiz.getDescription());
        assertEquals("HISTORY", quiz.getCategory());
        assertEquals("/icons/history.png", quiz.getIconUrl());
        assertEquals(150, quiz.getTimeLimitSeconds());
        assertEquals(2, quiz.getQuestions().size());
        assertEquals("Q1", quiz.getQuestions().get(0));
        assertEquals(2, quiz.getImages().size());
        assertEquals("/img1.png", quiz.getImages().get(0));
        assertNull(quiz.getCreatedAt());
    }

    public void test2() {
        Quiz quiz = new Quiz();
        assertNotNull(quiz.getQuestions());
        assertNotNull(quiz.getImages());
        assertEquals(300, quiz.getTimeLimitSeconds());
    }
}