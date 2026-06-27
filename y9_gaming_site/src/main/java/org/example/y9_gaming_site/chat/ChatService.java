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
    private ChatroomMemberRepository chatroomMemberRepository;

    public ChatService(MessageRepository messageRepository, ChatroomRepository chatroomRepository, FriendshipRepository friendshipRepository,ChatroomMemberRepository chatroomMemberRepository) {
        this.messageRepository = messageRepository;
        this.chatroomRepository = chatroomRepository;
        this.friendshipRepository = friendshipRepository;
        this.chatroomMemberRepository=chatroomMemberRepository;
    }

    // Opens a new group or lobby chatroom and adds the creator
    public ChatRoom createGroupRoom(String roomName, String type, List<Long> memberIds) {
        ChatRoom chatRoom = new ChatRoom(roomName, type);
        ChatRoom savedRoom = chatroomRepository.save(chatRoom);

        // Add all specified members to the room
        for (Long userId : memberIds) {
            chatroomMemberRepository.save(new ChatroomMember(savedRoom.getId(), userId));
        }
        return savedRoom;
    }

    // Specifically for 1-on-1 private messaging between friends
    public ChatRoom openPrivateRoom(Long user1Id, Long user2Id) {
        if (!isFriend(user1Id, user2Id)) {
            throw new RuntimeException("Users are not friends");
        }

        // Create a basic private chatroom container
        ChatRoom chatRoom = new ChatRoom(null, "PRIVATE");
        ChatRoom savedRoom = chatroomRepository.save(chatRoom);

        // Bind both friends to it
        chatroomMemberRepository.save(new ChatroomMember(savedRoom.getId(), user1Id));
        chatroomMemberRepository.save(new ChatroomMember(savedRoom.getId(), user2Id));

        return savedRoom;
    }


    private boolean isFriend(Long user1Id, Long user2Id) {
        return friendshipRepository.findBySenderIdAndReceiverId(user1Id, user2Id) != null
                || friendshipRepository.findBySenderIdAndReceiverId(user2Id, user1Id)!=null;

    }

    public Message messageSender(Long senderId, Long roomId, String message) {
        // Validation: Verify the sender actually belongs to this room
        chatroomMemberRepository.findByRoomIdAndUserId(roomId, senderId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this chatroom"));

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













