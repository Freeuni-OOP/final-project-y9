package org.example.y9_gaming_site.chat;

public class SendMessageDto {
    public Long senderId;
    public Long roomId;
    public String message;

    public SendMessageDto() {}

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
}
