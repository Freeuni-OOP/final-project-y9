package org.example.y9_gaming_site.Challenge;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = GameChallengeController.class)
public class GameChallengeExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage();

        if (msg.contains("not friends") || msg.contains("only receiver")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);
        }
        if (msg.startsWith("No Challenge found") || msg.startsWith("No record found") || msg.startsWith("No User found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
        }
        if (msg.contains("waiting for response") || msg.contains("expired")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(msg);
        }
        return ResponseEntity.badRequest().body(msg);
    }
}
