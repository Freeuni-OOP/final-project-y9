package org.example.y9_gaming_site.quiz;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Quiz>> getQuizzesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(quizService.getQuizzesByCategory(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
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
                             RedirectAttributes redirectAttributes) throws IOException {

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
                questionTexts, correctAnswers, wrongAnswers, imagePaths);

        redirectAttributes.addFlashAttribute("message", "Quiz published!");
        return "redirect:/home";
    }
}