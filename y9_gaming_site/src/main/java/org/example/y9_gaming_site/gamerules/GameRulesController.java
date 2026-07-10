package org.example.y9_gaming_site.gamerules;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules")
@CrossOrigin(origins = "*")
public class GameRulesController {

    private final GameRulesProvider gameRulesProvider;

    public GameRulesController(GameRulesProvider gameRulesProvider) {
        this.gameRulesProvider = gameRulesProvider;
    }

    @GetMapping("/{gameKey}")
    public ResponseEntity<GameRules> getRules(@PathVariable String gameKey) {
        try {
            return ResponseEntity.ok(gameRulesProvider.getRules(gameKey.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
