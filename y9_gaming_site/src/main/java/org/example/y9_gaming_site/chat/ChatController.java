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

    @PostMapping("/open/{user1Id}/{user2Id}/{type}")
    public ChatRoom openRoom(@PathVariable Long user1Id, @PathVariable Long user2Id, @PathVariable String type) {
        return chatService.openRoom(user1Id, user2Id, type);
    }
}
