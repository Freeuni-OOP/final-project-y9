package org.example.y9_gaming_site.chat;

import org.example.y9_gaming_site.friendship.Friendship;
import org.example.y9_gaming_site.friendship.FriendshipRepository;
import org.example.y9_gaming_site.notification.NotificationRepository;
import org.example.y9_gaming_site.notification.NotificationService;
import org.example.y9_gaming_site.security.ContentModerator;
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
    private static final String VIOLATION_MESSAGE = "This message violated our guidelines";

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

    public ChatRoom createGroupRoom(String roomName, String type, List<Long> memberIds) {
        ChatRoom chatRoom = new ChatRoom(roomName, type);
        ChatRoom savedRoom = chatroomRepository.save(chatRoom);

        for (Long userId : memberIds) {
            chatroomMemberRepository.save(new ChatroomMember(savedRoom.getId(), userId));
        }
        return savedRoom;
    }

    public ChatRoom openPrivateRoom(Long user1Id, Long user2Id) {
        if (!isFriend(user1Id, user2Id)) {
            throw new RuntimeException("Users are not friends");
        }

        ChatRoom chatExisting = findExistingPrivateRoom(user1Id, user2Id);
        if (chatExisting != null) {
            return chatExisting;
        }

        ChatRoom chatRoom = new ChatRoom(null, "PRIVATE");
        ChatRoom savedRoom = chatroomRepository.save(chatRoom);

        chatroomMemberRepository.save(new ChatroomMember(savedRoom.getId(), user1Id));
        chatroomMemberRepository.save(new ChatroomMember(savedRoom.getId(), user2Id));

        return savedRoom;
    }

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
        chatroomMemberRepository.findByRoomIdAndUserId(roomId, senderId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this chatroom"));

        List<ChatroomMember> members = chatroomMemberRepository.findByRoomId(roomId);

        boolean hasMinorRecipient = false;
        for (ChatroomMember member : members) {
            if (!member.getUserId().equals(senderId)) {
                User recipient = userRepository.findById(member.getUserId()).orElse(null);
                if (recipient != null && isMinor(recipient.getBirthDate())) {
                    hasMinorRecipient = true;
                    break;
                }
            }
        }

        boolean flagged = hasMinorRecipient && ContentModerator.isFlagged(message);

        Message messageEntity = new Message();
        messageEntity.setSenderId(senderId);
        messageEntity.setRoomId(roomId);
        messageEntity.setMessage(message);
        messageEntity.setTimestamp(LocalDateTime.now());
        messageEntity.setFlagged(flagged);
        Message savedMessage = messageRepository.save(messageEntity);

        for (ChatroomMember member : members) {
            if(!member.getUserId().equals(senderId)) {
                notificationService.createMessageNotification(senderId, member.getUserId(), roomId);
            }
        }

        if (flagged) {
            savedMessage.setMessage(VIOLATION_MESSAGE);
        }
        return savedMessage;
    }

    private boolean isMinor(java.time.LocalDate birthDate) {
        if (birthDate == null) {
            return false;
        }
        return java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears() < 18;
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
            dto.put("message", message.isFlagged() ? VIOLATION_MESSAGE : message.getMessage());
            dto.put("flagged", message.isFlagged());
            dto.put("timestamp", message.getTimestamp());
            result.add(dto);
        }
        return result;
    }
}