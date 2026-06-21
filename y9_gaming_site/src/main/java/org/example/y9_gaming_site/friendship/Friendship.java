package org.example.y9_gaming_site.friendship;


import jakarta.persistence.*;


@Entity
@Table(name = "friendships")
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String status;


    public Friendship() {


    }
    public Friendship(Long senderId, Long receiverId, String status) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
    }


    public Long getId () {
        return id;
    }
    public Long getSenderId () {
        return senderId;
    }
    public void setSenderId (Long senderId){
        this.senderId = senderId;
    }
    public Long getReceiverId () {
        return receiverId;
    }
    public void setReceiverId (Long receiverId){
        this.receiverId = receiverId;
    }
    public String getStatus () {
        return status;
    }
    public void setStatus (String status){
        this.status = status;
    }
}





