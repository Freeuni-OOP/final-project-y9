package org.example.y9_gaming_site.quiz;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuizService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public QuizService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Quiz> getAllQuizzes() {
        String sql = "SELECT * FROM quizzes";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Quiz q = new Quiz();
            q.setId(rs.getLong("id"));
            q.setTitle(rs.getString("title"));
            q.setCategory(rs.getString("category"));
            q.setDescription(rs.getString("description"));
            q.setTimeLimitSeconds(rs.getInt("time_limit_seconds"));

            String blob = rs.getString("questions_blob");
            q.setQuestions(blob != null && !blob.isBlank()
                    ? List.of(blob.split(";", -1)) : new ArrayList<>());

            String imgJson = rs.getString("images");
            List<String> images;
            try {
                images = (imgJson != null && !imgJson.isBlank())
                        ? objectMapper.readValue(imgJson, new TypeReference<List<String>>() {})
                        : new ArrayList<>();
            } catch (Exception e) {
                images = new ArrayList<>();
            }
            q.setImages(images);
            return q;
        });
    }

    public List<Quiz> getQuizzesByCategory(String category) {
        return getAllQuizzes().stream()
                .filter(q -> q.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    public Quiz getQuizById(Long id) {
        return getAllQuizzes().stream()
                .filter(quiz -> quiz.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Quiz ID " + id + " not found."));
    }



    public void createQuiz(String title, String category, String description, int timeLimit,
                           List<String> questionTexts, List<String> correctAnswers,
                           List<String> wrongAnswers, List<String> imagePaths) {

        List<String> questions = new ArrayList<>();
        for (int i = 0; i < questionTexts.size(); i++) {
            String wrong = wrongAnswers.get(i);
            String correct = correctAnswers.get(i);
            String qText = questionTexts.get(i);

            // If there are no wrong answers (Written Answer), don't add the "|"
            if (wrong == null || wrong.trim().isEmpty() || "NO_WRONG_ANSWERS".equals(wrong)) {
                questions.add(qText + " (" + correct + ")");
            } else {
                // Standard MCQ formatting
                questions.add(qText + " (" + correct + "|" + wrong + ")");
            }
        }
        String questionsBlob = String.join(";", questions);

        String imagesJson = imagePaths.stream()
                .map(p -> "\"" + (p == null ? "" : p.replace("\\", "\\\\").replace("\"", "\\\"")) + "\"")
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));

        String sql = "INSERT INTO quizzes (title, category, description, time_limit_seconds, questions_blob, images, created_at) VALUES (?, ?, ?, ?, ?, ?, NOW())";
        jdbcTemplate.update(sql, title, category, description, timeLimit, questionsBlob, imagesJson);
    }
    public void deleteQuiz(Long quizId) {
        String sql = "DELETE FROM quizzes WHERE id = ?";
        jdbcTemplate.update(sql, quizId);
    }
}