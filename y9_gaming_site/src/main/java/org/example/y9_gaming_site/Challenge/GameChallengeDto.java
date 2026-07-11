package org.example.y9_gaming_site.Challenge;

import java.time.LocalDateTime;

public record GameChallengeDto(
        Long id,
        Long senderId,
        String senderUsername,
        Long receiverId,
        String receiverUsername,
        String gameKey,
        Long contextId,
        double targetValue,
        Double resultValue,
        Long winnerId,
        String winnerUsername,
        String status,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime resolvedAt
) {
    public static GameChallengeDto from(GameChallenge c) {
        boolean resolved = c.getResRecord() != null;
        boolean hasWinner = c.getWinner() != null;
        return new GameChallengeDto(
                c.getId(),
                c.getSender().getId(),
                c.getSender().getUsername(),
                c.getReceiver().getId(),
                c.getReceiver().getUsername(),
                c.getTargRecord().getGame().getTitle(),
                c.getTargRecord().getContextId(),
                c.getTargRecord().getValue(),
                resolved ? c.getResRecord().getValue() : null,
                hasWinner ? c.getWinner().getId() : null,
                hasWinner ? c.getWinner().getUsername() : null,
                c.getStatus().name(),
                c.getCreatedAt(),
                c.getExpiresAt(),
                c.getResolvedAt()
        );
    }
}
