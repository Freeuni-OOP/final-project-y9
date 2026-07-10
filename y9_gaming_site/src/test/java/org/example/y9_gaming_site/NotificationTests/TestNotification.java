package org.example.y9_gaming_site.NotificationTests;

import junit.framework.TestCase;
import org.example.y9_gaming_site.friendship.Friendship;
import org.example.y9_gaming_site.friendship.FriendshipRepository;
import org.example.y9_gaming_site.notification.Notification;
import org.example.y9_gaming_site.notification.NotificationRepository;
import org.example.y9_gaming_site.notification.NotificationService;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class TestNotification extends TestCase {

    private NotificationRepository mockNotificationRepository;
    private FriendshipRepository mockFriendshipRepository;
    private UserRepository mockUserRepository;
    private NotificationService notificationService;

    private User sampleUser;
    private Notification sampleFriendNotification;
    private Notification sampleMessageNotification;
    private Friendship sampleFriendship;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockNotificationRepository = Mockito.mock(NotificationRepository.class);
        mockFriendshipRepository = Mockito.mock(FriendshipRepository.class);
        mockUserRepository = Mockito.mock(UserRepository.class);

        notificationService = new NotificationService(
                mockNotificationRepository,
                mockFriendshipRepository,
                mockUserRepository
        );

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("lasha");

        sampleFriendNotification = new Notification(2L, 1L, "FRIEND_REQUEST", "lasha sent you a friend request", 50L);
        sampleFriendNotification.setId(10L);

        sampleMessageNotification = new Notification(2L, 1L, "NEW_MESSAGE", "lasha sent you a message", 5L, false);
        sampleMessageNotification.setId(11L);

        sampleFriendship = new Friendship(1L, 2L, "PENDING");
    }

    public void testCreateFriendRequest() {
        when(mockUserRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(mockNotificationRepository.save(any(Notification.class))).thenReturn(sampleFriendNotification);

        notificationService.createFriendRequest(1L, 2L, 50L);

        verify(mockNotificationRepository, times(1)).save(any(Notification.class));
    }

    public void testCreateMessageNotification() {
        when(mockUserRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(mockNotificationRepository.save(any(Notification.class))).thenReturn(sampleMessageNotification);

        notificationService.createMessageNotification(1L, 2L, 5L);

        verify(mockNotificationRepository, times(1)).save(any(Notification.class));
    }

    public void testGetNotifications() {
        when(mockNotificationRepository.findByUserIdOrderByDateTimeDesc(2L))
                .thenReturn(Arrays.asList(sampleFriendNotification, sampleMessageNotification));

        List<Notification> result = notificationService.getNotifications(2L);

        assertEquals(2, result.size());
        assertEquals("FRIEND_REQUEST", result.get(0).getType());
    }

    public void testGetUnreadCount() {
        when(mockNotificationRepository.countByUserIdAndIsRead(2L, false)).thenReturn(5);

        int count = notificationService.getUnreadCount(2L);

        assertEquals(5, count);
    }

    public void testAcceptFriendship_Success() {
        when(mockNotificationRepository.findById(10L)).thenReturn(Optional.of(sampleFriendNotification));
        when(mockFriendshipRepository.findById(50L)).thenReturn(Optional.of(sampleFriendship));

        notificationService.acceptFriendship(10L);

        assertEquals("ACCEPTED", sampleFriendship.getStatus());
        verify(mockNotificationRepository, times(1)).delete(sampleFriendNotification);
    }

    public void testAcceptFriendship_NotificationNotFound() {
        when(mockNotificationRepository.findById(99L)).thenReturn(Optional.empty());

        try {
            notificationService.acceptFriendship(99L);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("Notification not found", e.getMessage());
        }
    }

    public void testAcceptFriendship_FriendshipIdNull() {
        sampleFriendNotification.setFriendshipId(null);
        when(mockNotificationRepository.findById(10L)).thenReturn(Optional.of(sampleFriendNotification));

        try {
            notificationService.acceptFriendship(10L);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("Friendship not found", e.getMessage());
        }
    }

    public void testDeclineFriendship_Success() {
        when(mockNotificationRepository.findById(10L)).thenReturn(Optional.of(sampleFriendNotification));
        when(mockFriendshipRepository.findById(50L)).thenReturn(Optional.of(sampleFriendship));

        notificationService.declineFriendship(10L);

        verify(mockFriendshipRepository, times(1)).delete(sampleFriendship);
        verify(mockNotificationRepository, times(1)).delete(sampleFriendNotification);
    }

    public void testDeclineFriendship_NotificationNotFound() {
        when(mockNotificationRepository.findById(99L)).thenReturn(Optional.empty());

        try {
            notificationService.declineFriendship(99L);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("Notification not found", e.getMessage());
        }
    }

    public void testMarkRead() {
        doNothing().when(mockNotificationRepository).markAllAsReadByUserId(2L);

        notificationService.markRead(2L);

        verify(mockNotificationRepository, times(1)).markAllAsReadByUserId(2L);
    }
}