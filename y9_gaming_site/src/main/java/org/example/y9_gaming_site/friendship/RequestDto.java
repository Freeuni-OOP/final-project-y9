package org.example.y9_gaming_site.friendship;


public class RequestDto {
    private Long senderId;
    private Long receiverId;


    public RequestDto() {
    }
    public Long getSenderId() {
        return senderId;
    }
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }
    public Long getReceiverId() {
        return receiverId;
    }
    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }
}
