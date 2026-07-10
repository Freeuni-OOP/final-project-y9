package org.example.y9_gaming_site.quiz;

import org.example.y9_gaming_site.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;
    private static final String UPLOAD_DIR = "uploads/quiz-images/";

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public ResponseEntity<List<QuizSummary>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizSummaries());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<QuizSummary>> getQuizzesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(quizService.getQuizSummariesByCategory(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<QuizCompletionResponse> completeQuiz(@PathVariable Long id, @RequestBody QuizCompletionRequest request,
                                                               Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(401).build();
        }
        Long userId = ((User) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(quizService.submitCompletion(userId, request.correctCount(), request.totalQuestions()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        // Optional: Add role validation checking here if your JwtAuthFilter doesn't handle it globally
        try {
            quizService.deleteQuiz(id);
            return ResponseEntity.ok().body("{\"message\": \"Quiz deleted successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"Failed to delete quiz\"}");
        }
    }

    @PostMapping("/new")
    public String createQuiz(@RequestParam String title,
                             @RequestParam String category,
                             @RequestParam String description,
                             @RequestParam int timeLimit,
                             @RequestParam("questionText") List<String> questionTexts,
                             @RequestParam("correctAnswer") List<String> correctAnswers,
                             @RequestParam("wrongAnswers") List<String> wrongAnswers,
                             @RequestParam("questionImage") List<MultipartFile> images,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) throws IOException {

        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new RuntimeException("Unauthorized user attempting to create a quiz.");
        }
        Long creatorId = ((User) authentication.getPrincipal()).getId();

        Files.createDirectories(Paths.get(UPLOAD_DIR));
        List<String> imagePaths = new ArrayList<>();

        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) {
                imagePaths.add("");
                continue;
            }
            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path target = Paths.get(UPLOAD_DIR, filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            imagePaths.add("/uploads/quiz-images/" + filename);
        }

        quizService.createQuiz(title, category, description, timeLimit,
                questionTexts, correctAnswers, wrongAnswers, imagePaths, creatorId);

        redirectAttributes.addFlashAttribute("message", "Quiz published!");
        return "redirect:/home";
    }

    @GetMapping("/my")
    public ResponseEntity<List<QuizSummary>> getMyQuizzes(Authentication authentication) {

        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(401).build();
        }

        Long creatorId = ((User) authentication.getPrincipal()).getId();

        return ResponseEntity.ok(quizService.getQuizSummariesByCreator(creatorId));
    }
}