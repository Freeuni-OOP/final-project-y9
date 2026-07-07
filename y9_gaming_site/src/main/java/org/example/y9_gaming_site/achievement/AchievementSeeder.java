package org.example.y9_gaming_site.achievement;

import org.springframework.boot.CommandLineRunner;

import java.util.ArrayList;
import java.util.List;

public class AchievementSeeder implements CommandLineRunner {

    private AchievementRepository achievementRepository;

    public AchievementSeeder(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    private record Def(String code, String name, String description){}


    @Override
    public void run(String... args) {
        List<Def> defs = List.of(
                // Wordle Achievements
                new Def("WORDLE_FIRST_WIN", "პირველი გამარჯვება", "მოიგე ვორდლი პირველად"),
                new Def("WORDLE_IN_TWO_TRIES", "იშვიათი მოგება", "გამოიცანი სიტყვა 2 ცდაში") ,
                new Def("WORDLE_IN_ONE_TRY", "შეუძლებელი", "გამოიცანი სიტყვა ერთი ცდით"),

                // joker Achievements
                new Def("JOKER_FIRST_GAME", "ჯოკერის პირველი თამაში", "დაასრულე ჯოკერის თამაში"),
                new Def("JOKER_10K", "ათი ათასი", "დააგროვე 10 000+ ქულა ჯოკერში"),
                new Def("JOKER_20K", "ოცი ათასი", "დააგროვე 20 000+ ქულა ჯოკერში"),

                // Sudoku
                new Def("SUDOKU_FIRST_SOLVE", "დამწყები", "ამოხსენი ნებისმიერი სუდოკუ"),
                new Def("SUDOKU_IN_120", "სხარტი", "ამოხსენი სუდოკუ 2 წუთზე ნაკლებში"),
                new Def("SUDOKU_IN_60", "wtf dude", "ამოხსენი სუდოკუ 1 წუთზე ნაკლებში"),

                // Quiz-ები
                new Def("QUIZ_FIRST", "ცოდნის მაძიებელი", "დაასრულე ქვიიზი"),
                new Def("QUIZ_PERFECT", "მცოდნე", "მიიღე მასიმალური ქულა ქვიზში")
                );
        int added = 0;
        for(Def def : defs){
            if(achievementRepository.findByCode(def.code()).isEmpty()){
                Achievement a = new Achievement();
                a.setCode(def.code());
                a.setName(def.name());
                a.setDescription(def.description());
                achievementRepository.save(a);
                added++;
            }
        }
    }
}
