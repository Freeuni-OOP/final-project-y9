package org.example.y9_gaming_site.notification;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long senderId;
    private String type;
    private String message;
    private boolean isRead;
    private LocalDateTime dateTime;

    public Notification() {}

    public Notification(Long userId, Long senderId, String type, String message) {
        this.userId = userId;
        this.senderId = senderId;
        this.type = type;
        this.message = message;
        this.isRead = false;
        this.dateTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public Long getUserId() {
        return userId;
    }
    public Long getSenderId() {
        return senderId;
    }
    public String getType() {
        return type;
    }
    public String getMessage() {
        return message;
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
}

