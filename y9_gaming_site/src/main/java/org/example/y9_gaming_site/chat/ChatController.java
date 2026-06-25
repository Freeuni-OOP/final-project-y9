package org.example.y9_gaming_site.chat;

import org.example.y9_gaming_site.chat.Message;
import org.example.y9_gaming_site.chat.ChatService;
import org.example.y9_gaming_site.chat.SendMessageDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public Message sendMessage(@RequestBody SendMessageDto sendMessageDto) {
        return chatService.messageSender(sendMessageDto.getSenderId(), sendMessageDto.getRoomId(), sendMessageDto.getMessage());
    }

    @GetMapping("/{roomId}")
    public List<Message> getMessages(@PathVariable Long roomId) {
        return chatService.getMessages(roomId);
    }

    // Endpoint for 1-on-1 friend chat
    @PostMapping("/open-private/{user1Id}/{user2Id}")
    public ChatRoom openPrivateRoom(@PathVariable Long user1Id, @PathVariable Long user2Id) {
        return chatService.openPrivateRoom(user1Id, user2Id);
    }

    // Endpoint for making group rooms (e.g., game lobbies with multiple user IDs)
    @PostMapping("/create-group")
    public ChatRoom createGroupRoom(@RequestParam String name,
                                    @RequestParam String type,
                                    @RequestBody List<Long> memberIds) {
        return chatService.createGroupRoom(name, type, memberIds);
    }
}
