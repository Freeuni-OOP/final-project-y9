package org.example.y9_gaming_site.chat;

import org.example.y9_gaming_site.friendship.FriendshipRepository;
import org.springframework.stereotype.Service;
import org.example.y9_gaming_site.chat.MessageRepository;
import org.example.y9_gaming_site.chat.Message;
import org.example.y9_gaming_site.chat.ChatroomRepository;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class ChatService {
    private final MessageRepository messageRepository;
    private final ChatroomRepository chatroomRepository;
    private final FriendshipRepository friendshipRepository;

    public ChatService(MessageRepository messageRepository, ChatroomRepository chatroomRepository, FriendshipRepository friendshipRepository) {
        this.messageRepository = messageRepository;
        this.chatroomRepository = chatroomRepository;
        this.friendshipRepository = friendshipRepository;
    }

    public ChatRoom openRoom(Long user1Id, Long user2Id, String type) {
        if(type.equals("friend") && !isFriend(user1Id,user2Id)) {
            throw new RuntimeException("users are not friends");
        }
        ChatRoom chatRoom = findRoom(user1Id, user2Id);
        if(chatRoom != null) {
            return chatRoom;
        }
        chatRoom = new ChatRoom(user1Id, user2Id, type);
        return chatroomRepository.save(chatRoom);
    }

    private boolean isFriend(Long user1Id, Long user2Id) {
        return friendshipRepository.findByUserIdAndFriendId(user1Id, user2Id) != null || friendshipRepository.findByUserIdAndFriendId(user2Id, user1Id) != null;
    }

    private ChatRoom findRoom(Long user1Id, Long user2Id) {
        ChatRoom chatRoom = chatroomRepository.findByUser1IdAndUser2Id(user1Id,user2Id);
        if(chatRoom == null) {
            chatRoom = chatroomRepository.findByUser1IdAndUser2Id(user1Id,user2Id);
        }
        return chatRoom;
    }


    public Message messageSender(Long senderId,Long roomId,String message){
        ChatRoom chatRoom = chatroomRepository.findById(roomId).orElse(null);
        if(chatRoom == null) {
            throw new RuntimeException("room not found");
        }
        boolean isMember = senderId.equals(chatRoom.getUser1Id()) || senderId.equals(chatRoom.getUser2Id());
        if(!isMember) {
            throw new RuntimeException("you are not member of this room");
        }

        Message messageEntity = new Message();
        messageEntity.setSenderId(senderId);
        messageEntity.setRoomId(roomId);
        messageEntity.setMessage(message);
        messageEntity.setTimestamp(LocalDateTime.now());
        return messageRepository.save(messageEntity);
    }

    public List<Message> getMessages(Long roomId){
        return messageRepository.findByRoomId(roomId);
    }
}













