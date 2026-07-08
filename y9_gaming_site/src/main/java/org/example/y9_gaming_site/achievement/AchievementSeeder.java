package org.example.y9_gaming_site.achievement;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
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
                new Def("WORDLE_DAILY_STREAK_10", "ურყევი", "მოიგე დღის ვორდლი ზედიზედ 10 დღე"),
                new Def("WORDLE_WINS_50", "ვეტერანი", "მოიგე დღის ვორდლი 50-ჯერ"),
                new Def("WORDLE_FLAWLESS", "უშეცდომოდ", "მოიგე თამაში ისე რომ არცერთი ასო არ იყოს ყვითელი"),
                // joker Achievements
                new Def("JOKER_FIRST_GAME", "ჯოკერის პირველი თამაში", "დაასრულე ჯოკერის თამაში"),
                new Def("JOKER_10K", "ათი ათასი", "დააგროვე 10 000+ ქულა ჯოკერში"),
                new Def("JOKER_20K", "ოცი ათასი", "დააგროვე 20 000+ ქულა ჯოკერში"),
                new Def("JOKER_WIN_STREAK_10", "ტუზი", "მოიგე ჯოკერის თამაში ზედიზედ 10 ჯერ"),
                new Def("JOKER_GAMES_50", "ჯოკერის ვეტერანი", "დაასრულე 50 ჯოკერის თამაში"),
                new Def("JOKER_PERFECT_BID", "მკითხავი", "ერთ თამაშში ყველა რაუნდში ზუსტად შეასრულე ნათქვამი"),
                // Sudoku
                new Def("SUDOKU_FIRST_SOLVE", "დამწყები", "ამოხსენი ნებისმიერი სუდოკუ"),
                new Def("SUDOKU_IN_120", "სხარტი", "ამოხსენი სუდოკუ 2 წუთზე ნაკლებში"),
                new Def("SUDOKU_IN_60", "wtf dude", "ამოხსენი სუდოკუ 1 წუთზე ნაკლებში"),
                new Def("SUDOKU_DAILY_STREAK_10", "ოსტატი", "ამოხსენი დღის სუდოკუ ზედიზედ 10 დღე"),
                new Def("SUDOKU_SOLVED_50", "სუდოკუს ვეტერანი", "ამოხსენი დღის სუდოკუ 50"),

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
