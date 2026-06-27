//package org.example.y9_gaming_site;
//
//import junit.framework.TestCase;
//import org.example.y9_gaming_site.quiz.Quiz;
//import org.example.y9_gaming_site.quiz.QuizRepository;
//import org.example.y9_gaming_site.quiz.QuizService;
//import org.mockito.Mockito;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//public class QuizTests extends TestCase {
//
//    private QuizRepository mockRepository;
//    private QuizService quizService;
//    private Quiz quiz1;
//    private Quiz quiz2;
//
//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//
//        mockRepository = Mockito.mock(QuizRepository.class);
//        quizService = new QuizService(mockRepository);
//
//        quiz1 = new Quiz();
//        quiz1.setId(1L);
//        quiz1.setTitle("Capitals of Europe");
//        quiz1.setCategory("GEOGRAPHY");
//        quiz1.setTimeLimitSeconds(120);
//
//        quiz2 = new Quiz();
//        quiz2.setId(2L);
//        quiz2.setTitle("Atomic Masses");
//        quiz2.setCategory("SCIENCE");
//        quiz2.setTimeLimitSeconds(180);
//    }
//
//    public void test1() {
//        Mockito.when(mockRepository.findAll()).thenReturn(Arrays.asList(quiz1, quiz2));
//
//        List<Quiz> result = quizService.getAllQuizzes();
//
//        assertEquals(2, result.size());
//        assertEquals("Capitals of Europe", result.get(0).getTitle());
//    }
//
//    public void test2() {
//        Mockito.when(mockRepository.findByCategory("GEOGRAPHY")).thenReturn(Arrays.asList(quiz1));
//
//        List<Quiz> result = quizService.getQuizzesByCategory("geography");
//
//        assertEquals(1, result.size());
//        assertEquals("GEOGRAPHY", result.get(0).getCategory());
//    }
//
//    public void test3() {
//        Mockito.when(mockRepository.findById(1L)).thenReturn(Optional.of(quiz1));
//
//        Quiz result = quizService.getQuizById(1L);
//
//        assertNotNull(result);
//        assertEquals(1L, result.getId().longValue());
//        assertEquals("Capitals of Europe", result.getTitle());
//    }
//
//    public void test4() {
//        Mockito.when(mockRepository.findById(99L)).thenReturn(Optional.empty());
//
//        try {
//            quizService.getQuizById(99L);
//            fail("Expected a RuntimeException to be thrown");
//        } catch (RuntimeException e) {
//            assertEquals("Quiz session index not found", e.getMessage());
//        }
//    }
//}