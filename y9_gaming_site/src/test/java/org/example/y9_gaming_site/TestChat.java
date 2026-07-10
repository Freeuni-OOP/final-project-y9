package org.example.y9_gaming_site;

import junit.framework.TestCase;
import org.example.y9_gaming_site.chat.*;
import org.example.y9_gaming_site.friendship.Friendship;
import org.example.y9_gaming_site.friendship.FriendshipRepository;
import org.example.y9_gaming_site.notification.NotificationService;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class TestChat extends TestCase {

    private MessageRepository messageRepository;
    private ChatroomRepository chatroomRepository;
    private FriendshipRepository friendshipRepository;
    private ChatroomMemberRepository chatroomMemberRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;

    private ChatService chatService;
    private User testUser1;
    private User testUser2;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        messageRepository = mock(MessageRepository.class);
        chatroomRepository = mock(ChatroomRepository.class);
        friendshipRepository = mock(FriendshipRepository.class);
        chatroomMemberRepository = mock(ChatroomMemberRepository.class);
        userRepository = mock(UserRepository.class);
        notificationService = mock(NotificationService.class);

        chatService = new ChatService(
                messageRepository,
                chatroomRepository,
                friendshipRepository,
                chatroomMemberRepository,
                userRepository,
                notificationService
        );

        testUser1 = new User();
        testUser1.setId(1L);
        testUser1.setUsername("user1");

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setUsername("user2");
    }


    public void testFindUserByUsername_Success() {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(testUser1));

        Map<String, Object> result = chatService.findUserByUsername("user1");

        assertNotNull(result);
        assertEquals(1L, result.get("id"));
        assertEquals("user1", result.get("username"));
    }

    public void testFindUserByUsername_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        try {
            chatService.findUserByUsername("unknown");
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("User not found", e.getMessage());
        }
    }


    public void testCreateGroupRoom() {
        ChatRoom mockRoom = new ChatRoom("Gamers", "GROUP");
        mockRoom.setId(10L);

        when(chatroomRepository.save(any(ChatRoom.class))).thenReturn(mockRoom);

        ChatRoom result = chatService.createGroupRoom("Gamers", "GROUP", List.of(1L, 2L));

        assertNotNull(result);
        assertEquals(10L, result.getId().longValue());
        assertEquals("Gamers", result.getName());
        verify(chatroomMemberRepository, times(2)).save(any(ChatroomMember.class));
    }


    public void testOpenPrivateRoom_NotFriends() {
        when(friendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(null);
        when(friendshipRepository.findBySenderIdAndReceiverId(2L, 1L)).thenReturn(null);

        try {
            chatService.openPrivateRoom(1L, 2L);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("Users are not friends", e.getMessage());
        }
    }

    public void testOpenPrivateRoom_ExistingChat() {
        Friendship friendship = new Friendship();
        friendship.setStatus("ACCEPTED");
        when(friendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(friendship);

        ChatroomMember member1 = new ChatroomMember(5L, 1L);
        when(chatroomMemberRepository.findByUserId(1L)).thenReturn(List.of(member1));

        ChatRoom existingRoom = new ChatRoom(null, "PRIVATE");
        existingRoom.setId(5L);
        when(chatroomRepository.findById(5L)).thenReturn(Optional.of(existingRoom));
        when(chatroomMemberRepository.findByRoomIdAndUserId(5L, 2L)).thenReturn(Optional.of(new ChatroomMember(5L, 2L)));

        ChatRoom result = chatService.openPrivateRoom(1L, 2L);

        assertNotNull(result);
        assertEquals(5L, result.getId().longValue());
        verify(chatroomRepository, never()).save(any(ChatRoom.class));
    }

    public void testOpenPrivateRoom_NewChat() {
        Friendship friendship = new Friendship();
        friendship.setStatus("ACCEPTED");

        when(friendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(null);
        when(friendshipRepository.findBySenderIdAndReceiverId(2L, 1L)).thenReturn(friendship);

        when(chatroomMemberRepository.findByUserId(1L)).thenReturn(new ArrayList<>());

        ChatRoom newRoom = new ChatRoom(null, "PRIVATE");
        newRoom.setId(7L);
        when(chatroomRepository.save(any(ChatRoom.class))).thenReturn(newRoom);

        ChatRoom result = chatService.openPrivateRoom(1L, 2L);

        assertNotNull(result);
        assertEquals(7L, result.getId().longValue());
        verify(chatroomMemberRepository, times(2)).save(any(ChatroomMember.class));
    }

    public void testMessageSender_NotMember() {
        when(chatroomMemberRepository.findByRoomIdAndUserId(100L, 1L)).thenReturn(Optional.empty());

        try {
            chatService.messageSender(1L, 100L, "Hello");
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("You are not a member of this chatroom", e.getMessage());
        }
    }

    public void testMessageSender_SuccessAndNotification() {
        ChatroomMember senderMember = new ChatroomMember(100L, 1L);
        when(chatroomMemberRepository.findByRoomIdAndUserId(100L, 1L)).thenReturn(Optional.of(senderMember));

        Message mockMessage = new Message();
        mockMessage.setSenderId(1L);
        mockMessage.setRoomId(100L);
        mockMessage.setMessage("Hello guys");
        when(messageRepository.save(any(Message.class))).thenReturn(mockMessage);

        ChatroomMember member1 = new ChatroomMember(100L, 1L);
        ChatroomMember member2 = new ChatroomMember(100L, 2L);
        when(chatroomMemberRepository.findByRoomId(100L)).thenReturn(List.of(member1, member2));

        Message result = chatService.messageSender(1L, 100L, "Hello guys");

        assertNotNull(result);
        assertEquals("Hello guys", result.getMessage());

        verify(notificationService, times(1)).createMessageNotification(1L, 2L, 100L);
        verify(notificationService, never()).createMessageNotification(1L, 1L, 100L);
    }

    public void testGetMessages() {
        Message m1 = new Message();
        m1.setId(101L);
        m1.setSenderId(1L);
        m1.setMessage("Msg 1");

        Message m2 = new Message();
        m2.setId(102L);
        m2.setSenderId(2L);
        m2.setMessage("Msg 2");

        Message m3 = new Message();
        m3.setId(103L);
        m3.setSenderId(3L);
        m3.setMessage("Msg 3");

        when(messageRepository.findByRoomId(100L)).thenReturn(List.of(m1, m2, m3));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser2));
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        List<Map<String, Object>> result = chatService.getMessages(100L);

        assertEquals(3, result.size());

        assertEquals("user1", result.get(0).get("senderUsername"));
        assertEquals("Msg 1", result.get(0).get("message"));

        assertEquals("user2", result.get(1).get("senderUsername"));

        assertEquals("unknown", result.get(2).get("senderUsername"));
    }
}