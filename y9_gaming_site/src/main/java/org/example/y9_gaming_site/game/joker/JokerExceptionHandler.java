package org.example.y9_gaming_site.game.joker;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = JokerController.class)
public class JokerExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        // "Game not found" ტიპის შეცდომები → 404
        if (ex.getMessage() != null && ex.getMessage().startsWith("Game not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ოთახი აღარ არსებობს ან წაშლილია.");
        }
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}