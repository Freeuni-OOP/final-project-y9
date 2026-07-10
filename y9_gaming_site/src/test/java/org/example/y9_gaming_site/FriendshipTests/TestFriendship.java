package org.example.y9_gaming_site.FriendshipTests;

import junit.framework.TestCase;
import org.example.y9_gaming_site.friendship.Friendship;
import org.example.y9_gaming_site.friendship.FriendshipRepository;
import org.example.y9_gaming_site.friendship.FriendshipService;
import org.example.y9_gaming_site.notification.NotificationService;
import org.example.y9_gaming_site.profile.UserProfileResponse;
import org.example.y9_gaming_site.user.Role;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class TestFriendship extends TestCase {

    private FriendshipRepository mockFriendshipRepository;
    private NotificationService mockNotificationService;
    private UserRepository mockUserRepository;
    private FriendshipService friendshipService;
    private Friendship sampleFriendship;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockFriendshipRepository = Mockito.mock(FriendshipRepository.class);
        mockNotificationService = Mockito.mock(NotificationService.class);
        mockUserRepository = Mockito.mock(UserRepository.class);

        friendshipService = new FriendshipService(mockFriendshipRepository, mockNotificationService, mockUserRepository);

        sampleFriendship = new Friendship(1L, 2L, "PENDING");
        java.lang.reflect.Field idField = Friendship.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(sampleFriendship, 100L);
    }

    public void test1() {
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(null);
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(2L, 1L)).thenReturn(null);
        when(mockFriendshipRepository.save(any(Friendship.class))).thenReturn(sampleFriendship);

        Friendship result = friendshipService.sendRequest(1L, 2L);

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        verify(mockNotificationService, times(1)).createFriendRequest(eq(1L), eq(2L), any());
    }

    public void test2() {
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(sampleFriendship);

        try {
            friendshipService.sendRequest(1L, 2L);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Friend request already exists", e.getMessage());
        }
    }

    public void test3() {
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(null);
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(2L, 1L)).thenReturn(sampleFriendship);

        try {
            friendshipService.sendRequest(1L, 2L);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Friend request already exists", e.getMessage());
        }
    }

    public void test4() {
        when(mockFriendshipRepository.findById(100L)).thenReturn(Optional.of(sampleFriendship));
        when(mockFriendshipRepository.save(any(Friendship.class))).thenReturn(sampleFriendship);

        Friendship result = friendshipService.acceptRequest(100L);

        assertNotNull(result);
        assertEquals("ACCEPTED", result.getStatus());
    }

    public void test5() {
        when(mockFriendshipRepository.findByReceiverIdAndStatus(2L, "PENDING"))
                .thenReturn(Arrays.asList(sampleFriendship));

        List<Friendship> result = friendshipService.getPendingRequests(2L);

        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
    }

    public void test6() {
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(null);
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(2L, 1L)).thenReturn(null);

        String status = friendshipService.getStatus(1L, 2L);
        assertEquals("NONE", status);
    }

    public void test7() {
        sampleFriendship.setStatus("ACCEPTED");
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(sampleFriendship);

        String status = friendshipService.getStatus(1L, 2L);
        assertEquals("FRIENDS", status);
    }

    public void test8() {
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(null);
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(2L, 1L)).thenReturn(sampleFriendship);

        String status = friendshipService.getStatus(1L, 2L);
        assertEquals("PENDING", status);
    }

    public void test9() {
        Friendship f1 = new Friendship(1L, 2L, "ACCEPTED");
        Friendship f2 = new Friendship(3L, 1L, "ACCEPTED");

        when(mockFriendshipRepository.findBySenderIdAndStatus(1L, "ACCEPTED")).thenReturn(List.of(f1));
        when(mockFriendshipRepository.findByReceiverIdAndStatus(1L, "ACCEPTED")).thenReturn(List.of(f2));

        List<Long> friendIds = friendshipService.getFriendIds(1L);

        assertEquals(2, friendIds.size());
        assertTrue(friendIds.contains(2L));
        assertTrue(friendIds.contains(3L));
    }

    public void test10() {
        when(mockFriendshipRepository.findBySenderIdAndStatus(1L, "ACCEPTED")).thenReturn(new ArrayList<>());
        when(mockFriendshipRepository.findByReceiverIdAndStatus(1L, "ACCEPTED")).thenReturn(new ArrayList<>());

        List<UserProfileResponse> responses = friendshipService.searchFriends(1L, "test");
        assertTrue(responses.isEmpty());
    }

    public void test11() {
        Friendship f1 = new Friendship(1L, 2L, "ACCEPTED");
        when(mockFriendshipRepository.findBySenderIdAndStatus(1L, "ACCEPTED")).thenReturn(List.of(f1));
        when(mockFriendshipRepository.findByReceiverIdAndStatus(1L, "ACCEPTED")).thenReturn(new ArrayList<>());

        User u2 = new User();
        u2.setId(2L);
        u2.setUsername("Alex_Gamer");
        u2.setAvatarUrl("/avatar.png");
        u2.setRole(Role.USER);

        when(mockUserRepository.findAllById(List.of(2L))).thenReturn(List.of(u2));

        List<UserProfileResponse> responses = friendshipService.searchFriends(1L, "alex");

        assertEquals(1, responses.size());
    }

    public void test12() {
        Friendship f1 = new Friendship(1L, 2L, "ACCEPTED");
        when(mockFriendshipRepository.findBySenderIdAndStatus(1L, "ACCEPTED")).thenReturn(List.of(f1));
        when(mockFriendshipRepository.findByReceiverIdAndStatus(1L, "ACCEPTED")).thenReturn(new ArrayList<>());

        User u2 = new User();
        u2.setId(2L);
        u2.setUsername("Alex_Gamer");
        u2.setAvatarUrl("/avatar.png");
        u2.setRole(Role.USER);

        when(mockUserRepository.findAllById(List.of(2L))).thenReturn(List.of(u2));

        List<UserProfileResponse> responses = friendshipService.searchFriends(1L, "bob");

        assertTrue(responses.isEmpty());
    }
}