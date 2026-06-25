package org.example.y9_gaming_site.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chatrooms")
@Getter
@Setter
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String name; //name of chat
    private String type; // "PRIVATE", "GROUP", "GAME_LOBBY"


    public ChatRoom() {}

    public ChatRoom(String name, String type){
        this.name=name;
        this.type=type;
    }


}











