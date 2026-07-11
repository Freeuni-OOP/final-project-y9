package org.example.y9_gaming_site.Challenge;

public class SendChallengeRequest {
    private Long receiverId;
    private String gameKey;
    private Long contextId;

    public SendChallengeRequest() {}

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getGameKey() { return gameKey; }
    public void setGameKey(String gameKey) { this.gameKey = gameKey; }

    public Long getContextId() { return contextId; }
    public void setContextId(Long contextId) { this.contextId = contextId; }
}
