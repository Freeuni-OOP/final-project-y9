package org.example.y9_gaming_site.gamerules;

import org.example.y9_gaming_site.game.sudoku.SudokuService;
import org.example.y9_gaming_site.game.wordle.WordleDict;
import org.example.y9_gaming_site.game.wordle.WordleService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
public class GameRulesProvider {

    private final Map<String, GameRules> rulesByGameKey;

    public GameRulesProvider() {
        rulesByGameKey = Map.of(
                WordleService.GAME_KEY, new GameRules(
                        WordleService.GAME_KEY,
                        "ვორდლი — წესები",
                        List.of(
                                "გამოიცანი დამალული " + WordleDict.WORD_LENGTh + "-ასოიანი სიტყვა 6 ცდაში.",
                                "ვარდისფერი ფონი — ასო სწორ ადგილასაა.",
                                "ოქროსფერი ფონი — ასო სიტყვაშია, მაგრამ არასწორ ადგილას.",
                                "მუქი ფონი — ასო საერთოდ არ გვხვდება სიტყვაში.",
                                "აირჩიე დღის თამაში ან ივარჯიშე შეუზღუდავად პრაქტიკის რეჟიმში."
                        )
                ),
                SudokuService.GAME_KEY, new GameRules(
                        SudokuService.GAME_KEY,
                        "Sudoku — Rules",
                        List.of(
                                "Fill the 9×9 grid so every row, column and 3×3 box has 1–9 exactly once.",
                                "You get 3 hints and 3 mistakes per puzzle — a 3rd mistake ends the game.",
                                "Toggle Auto Check to see errors highlighted as you type.",
                                "Play the daily board, or pick a difficulty in Infinite Mode.",
                                "Use Challenge Friend to send this exact board to someone else."
                        )
                )
        );
    }

    public GameRules getRules(String gameKey) {
        GameRules rules = rulesByGameKey.get(gameKey);
        if (rules == null) {
            throw new IllegalArgumentException("No such gameKey: " + gameKey);
        }
        return rules;
    }
}
