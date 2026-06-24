package org.example.y9_gaming_site.quiz;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class QuizDatabaseSeeder implements CommandLineRunner {

    //fills database with some starter quiz before we add quizzes from admin panel manually. uses imported databases


    private final JdbcTemplate jdbcTemplate;
    private final List<String> continents = List.of("Africa", "Europe", "Asia", "North America", "South America", "Oceania");

    public QuizDatabaseSeeder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM quizzes", Integer.class);
        if (count != null && count > 0) {
            System.out.println(">>> Quizzes table already contains records. Skipping auto-seeding engine.");
            return;
        }

        System.out.println(">>> Initializing Quiz Database Seeder Engine...");

        seedGeographyTyping("African Capitals", "Africa", 600);
        seedGeographyTyping("European Capitals", "Europe", 450);
        seedGeographyTyping("Asian Capitals", "Asia", 600);
        seedGeographyTyping("North American Capitals", "North America", 400);
        seedGeographyTyping("South American Capitals", "South America", 300);
        seedGeographyTyping("Capitals of Oceania", "Oceania", 300);
        seedGeographyTyping("Ultimate World Capitals", "GLOBAL", 1200);

        seedGeographyMCQCapital();
        seedGeographyMCQCountry();

        seedScienceQuiz("Elemental Symbols (MCQ)", 300, "Match the element full name directly to its symbol!", "MCQ_NAME_TO_SYMBOL");
        seedScienceQuiz("Identify the Element (MCQ)", 300, "Given a chemical symbol shorthand, identify the full name.", "MCQ_SYMBOL_TO_NAME");
        seedScienceQuiz("Periodic Table Blocks (MCQ)", 250, "Match the element to its correct block (s, p, d, f)!", "MCQ_BLOCK");
        seedScienceQuiz("Periodic Table Shorthand (Typing)", 400, "Type out the correct chemical symbol shorthand for each element.", "TEXT_SYMBOL");
        seedScienceQuiz("Atomic Number Race (Typing)", 300, "Given the name of an element, type its exact numerical Atomic Number!", "TEXT_ATOMIC_NUMBER");

        System.out.println(">>> Quiz Database seeding completed successfully!");
    }

    // making geography type-in quizzes
    private void seedGeographyTyping(String title, String continent, int seconds) {
        String sql = continent.equals("GLOBAL")
                ? "SELECT country, capital_city FROM world_capitals"
                : "SELECT country, capital_city FROM world_capitals WHERE continent LIKE '%" + continent + "%'";

        List<String> questions = jdbcTemplate.query(sql, (rs, r) ->
                "What is the capital of " + rs.getString("country") + "? (" + rs.getString("capital_city") + ")"
        );

        saveToDatabase(title, "GEOGRAPHY", "Test your geography skills!", seconds, questions);
    }

    //multiple choice question
    private void seedGeographyMCQCapital() {
        String sql = "SELECT capital_city, continent FROM world_capitals WHERE capital_city IS NOT NULL AND capital_city != '' ORDER BY RAND() LIMIT 20";
        List<String> questions = jdbcTemplate.query(sql, (rs, r) -> {
            String capital = rs.getString("capital_city");
            String correct = rs.getString("continent");
            List<String> wrongs = continents.stream().filter(c -> !c.equalsIgnoreCase(correct)).limit(3).toList();
            return "Which continent is the capital city '" + capital + "' located in? (" + correct + "|" + wrongs.get(0) + "|" + wrongs.get(1) + "|" + wrongs.get(2) + ")";
        });
        saveToDatabase("Match Capital to Continent (MCQ)", "GEOGRAPHY", "Pick the correct continent for the given capital city!", 300, questions);
    }

    private void seedGeographyMCQCountry() {
        String sql = "SELECT country, continent FROM world_capitals WHERE country IS NOT NULL ORDER BY RAND() LIMIT 20";
        List<String> questions = jdbcTemplate.query(sql, (rs, r) -> {
            String country = rs.getString("country");
            String correct = rs.getString("continent");
            List<String> wrongs = continents.stream().filter(c -> !c.equalsIgnoreCase(correct)).limit(3).toList();
            return "Which continent does the nation '" + country + "' belong to? (" + correct + "|" + wrongs.get(0) + "|" + wrongs.get(1) + "|" + wrongs.get(2) + ")";
        });
        saveToDatabase("Match Country to Continent (MCQ)", "GEOGRAPHY", "Pick the correct continent for the given sovereign nation!", 300, questions);
    }

    private void seedScienceQuiz(String title, int seconds, String desc, String mode) {
        String sql = "SELECT name, symbol, block, atomic_number FROM elements WHERE name IS NOT NULL AND symbol IS NOT NULL ORDER BY RAND() LIMIT 20";
        List<String> allSymbols = jdbcTemplate.query("SELECT symbol FROM elements", (rs, r) -> rs.getString("symbol"));
        List<String> allNames = jdbcTemplate.query("SELECT name FROM elements", (rs, r) -> rs.getString("name"));
        List<String> standardBlocks = List.of("s-block", "p-block", "d-block", "f-block");

        List<String> questions = jdbcTemplate.query(sql, (rs, rowNum) -> {
            String name = rs.getString("name");
            String symbol = rs.getString("symbol");
            String block = rs.getString("block") != null ? rs.getString("block").trim() + "-block" : "s-block";
            String atomicNum = rs.getString("atomic_number");

            switch (mode) {
                case "MCQ_NAME_TO_SYMBOL":
                    List<String> ws = allSymbols.stream().filter(s -> !s.equalsIgnoreCase(symbol)).distinct().limit(3).toList();
                    return "What is the chemical symbol for " + name + "? (" + symbol + "|" + ws.get(0) + "|" + ws.get(1) + "|" + ws.get(2) + ")";
                case "MCQ_SYMBOL_TO_NAME":
                    List<String> wn = allNames.stream().filter(n -> !n.equalsIgnoreCase(name)).distinct().limit(3).toList();
                    return "What element does the symbol '" + symbol + "' stand for? (" + name + "|" + wn.get(0) + "|" + wn.get(1) + "|" + wn.get(2) + ")";
                case "MCQ_BLOCK":
                    List<String> wb = standardBlocks.stream().filter(b -> !b.equalsIgnoreCase(block)).toList();
                    return "Which block on the periodic table does '" + name + "' belong to? (" + block + "|" + wb.get(0) + "|" + wb.get(1) + "|" + wb.get(2) + ")";
                case "TEXT_SYMBOL":
                    return "What is the chemical symbol for " + name + "? (" + symbol + ")";
                case "TEXT_ATOMIC_NUMBER":
                    return "What is the atomic number of " + name + "? (" + atomicNum + ")";
                default:
                    return "";
            }
        });

        saveToDatabase(title, "SCIENCE", desc, seconds, questions);
    }

    //stores all of these quizzes in quizzes db
    private void saveToDatabase(String title, String category, String desc, int seconds, List<String> questions) {
        String questionsBlob = String.join(";", questions);
        String sql = "INSERT INTO quizzes (title, category, description, time_limit_seconds, questions_blob, created_at) VALUES (?, ?, ?, ?, ?, NOW())";
        jdbcTemplate.update(sql, title, category, desc, seconds, questionsBlob);
    }
}