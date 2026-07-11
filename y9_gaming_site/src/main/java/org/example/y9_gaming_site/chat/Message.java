package org.example.y9_gaming_site.chat;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id")
    private Long senderId;
    @Column(name = "room_id")
    private Long roomId;
    @Column(name = "message")
    private String message;
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public Message() {}

    public Message(Long senderId, Long roomId, String message) {
        this.senderId = senderId;
        this.roomId = roomId;
        this.message = message;
    }
    public Long getId() {
        return id;
    }
    public  void setId(Long id) {this.id = id;}
    public Long getSenderId() {
        return senderId;
    }
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }
    public Long getRoomId() {
        return roomId;
    }
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Column(name = "flagged")
    private boolean flagged;

    public boolean isFlagged() {
        return flagged;
    }
    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }
}



