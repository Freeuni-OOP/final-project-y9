package org.example.y9_gaming_site.chat;

import jakarta.persistence.*;

@Entity
@Table(name = "chatrooms")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private Long user1Id;
    private Long user2Id;
    private String type;

    public ChatRoom() {}

    public ChatRoom(Long user1Id, Long user2Id, String type) {
        this.type = type;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
    }
    public Long getId() {
        return id;
    }
    public Long getUser1Id() {
        return user1Id;
    }
    public void setUser1Id(Long user1Id) {
        this.user1Id = user1Id;
    }
    public Long getUser2Id() {
        return user2Id;
    }
    public void setUser2Id(Long user2Id) {
        this.user2Id = user2Id;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

}











