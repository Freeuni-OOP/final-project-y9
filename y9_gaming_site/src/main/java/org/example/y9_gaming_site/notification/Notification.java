package org.example.y9_gaming_site.notification;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "sender_id", nullable = false)
    private Long senderId;
    @Column(name = "type", nullable = false)
    private String type;
    @Column(name = "message", nullable = false)
    private String message;
    @Column(name = "is_read")
    private boolean isRead;
    @Column(name = "date_time")
    private LocalDateTime dateTime;
    @Column(name = "friendship_id")
    private Long friendshipId;

    public Notification() {}

    public Notification(Long userId, Long senderId, String type, String message, Long friendshipId) {
        this.userId = userId;
        this.senderId = senderId;
        this.type = type;
        this.message = message;
        this.isRead = false;
        this.dateTime = LocalDateTime.now();
        this.friendshipId = friendshipId;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSenderId() {
        return senderId;
    }
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }
    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Long getFriendshipId() {
        return friendshipId;
    }
    public void setFriendshipId(Long friendshipId) {
        this.friendshipId = friendshipId;
    }
}

