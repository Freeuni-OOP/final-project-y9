package org.example.y9_gaming_site.chat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="chatroom_members")
@Getter
@Setter
public class ChatroomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();

    public ChatroomMember(){}

    public ChatroomMember(Long roomId, Long userId) {
        this.roomId = roomId;
        this.userId = userId;
    }

}
