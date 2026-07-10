package org.example.y9_gaming_site.chat;

import org.example.y9_gaming_site.friendship.Friendship;
import org.example.y9_gaming_site.friendship.FriendshipRepository;
import org.example.y9_gaming_site.notification.NotificationRepository;
import org.example.y9_gaming_site.notification.NotificationService;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.example.y9_gaming_site.chat.MessageRepository;
import org.example.y9_gaming_site.chat.Message;
import org.example.y9_gaming_site.chat.ChatroomRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ChatService {
    private final MessageRepository messageRepository;
    private final ChatroomRepository chatroomRepository;
    private final FriendshipRepository friendshipRepository;
    private ChatroomMemberRepository chatroomMemberRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;

    public ChatService(MessageRepository messageRepository, ChatroomRepository chatroomRepository, FriendshipRepository friendshipRepository, ChatroomMemberRepository chatroomMemberRepository, UserRepository userRepository, NotificationService notificationService) {
        this.messageRepository = messageRepository;
        this.chatroomRepository = chatroomRepository;
        this.friendshipRepository = friendshipRepository;
        this.chatroomMemberRepository = chatroomMemberRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    //finding user by username, returns id and username seperatly, not whole object
    public Map<String, Object> findUserByUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        return map;
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

        //if chat already exist, old chat will continue
        ChatRoom chatExisting = findExistingPrivateRoom(user1Id, user2Id);
        if (chatExisting != null) {
            return chatExisting;
        }

        // Create a basic private chatroom container
        ChatRoom chatRoom = new ChatRoom(null, "PRIVATE");
        ChatRoom savedRoom = chatroomRepository.save(chatRoom);

        // Bind both friends to it
        chatroomMemberRepository.save(new ChatroomMember(savedRoom.getId(), user1Id));
        chatroomMemberRepository.save(new ChatroomMember(savedRoom.getId(), user2Id));

        return savedRoom;
    }

    //if chat already exist, this method will return room
    private ChatRoom findExistingPrivateRoom(Long user1Id, Long user2Id) {
        List<ChatroomMember> roomofUser1 = chatroomMemberRepository.findByUserId(user1Id);
        for (ChatroomMember member : roomofUser1) {
            ChatRoom room = chatroomRepository.findById(member.getRoomId()).orElse(null);
            if (room != null && "PRIVATE".equals(room.getType())) {
                if (chatroomMemberRepository.findByRoomIdAndUserId(room.getId(), user2Id).isPresent()) {
                    return room;
                }
            }
        }
        return null;
    }

    private boolean isFriend(Long user1Id, Long user2Id) {
        Friendship friendship = friendshipRepository.findBySenderIdAndReceiverId(user1Id, user2Id);

        if (friendship != null && friendship.getStatus().equals("ACCEPTED")) {
            return true;
        }

        friendship = friendshipRepository.findBySenderIdAndReceiverId(user2Id, user1Id);

        if (friendship != null && friendship.getStatus().equals("ACCEPTED")) {
            return true;
        }

        return false;
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
        Message savedMessage =  messageRepository.save(messageEntity);

        List<ChatroomMember> members = chatroomMemberRepository.findByRoomId(roomId);
        for (ChatroomMember member : members) {
            if(!member.getUserId().equals(senderId)) {
                notificationService.createMessageNotification(senderId, member.getUserId(), roomId);
            }
        }
        return savedMessage;
    }

    public List<Map<String, Object>> getMessages(Long roomId) {
        List<Message> messages = messageRepository.findByRoomId(roomId);
        Map<Long, String> userName = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Message message : messages) {
            Long senderId = message.getSenderId();
            String username = userName.get(senderId);
            if (username == null) {
                User sender = userRepository.findById(message.getSenderId()).orElse(null);
                if (sender != null) {
                    username = sender.getUsername();
                } else {
                    username = "unknown";
                }
                userName.put(senderId, username);
            }
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", message.getId());
                dto.put("senderId", message.getSenderId());
                dto.put("senderUsername", username);
                dto.put("message", message.getMessage());
                dto.put("timestamp", message.getTimestamp());
                result.add(dto);
            }
            return result;
        }
}













