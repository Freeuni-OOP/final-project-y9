package org.example.y9_gaming_site.quiz;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuizService {

    private final JdbcTemplate jdbcTemplate;

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
            if (blob != null && !blob.isBlank()) {
                q.setQuestions(List.of(blob.split(";")));
            } else {
                q.setQuestions(new ArrayList<>());
            }
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
}