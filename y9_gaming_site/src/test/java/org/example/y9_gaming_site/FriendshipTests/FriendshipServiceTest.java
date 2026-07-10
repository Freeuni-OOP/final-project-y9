package org.example.y9_gaming_site.FriendshipTests;

import junit.framework.TestCase;
import org.example.y9_gaming_site.friendship.Friendship;
import org.example.y9_gaming_site.friendship.FriendshipRepository;
import org.example.y9_gaming_site.friendship.FriendshipService;
import org.example.y9_gaming_site.notification.NotificationService;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class FriendshipServiceTest extends TestCase {

    private FriendshipRepository mockFriendshipRepository;
    private NotificationService mockNotificationService;
    private FriendshipService friendshipService;
    private Friendship sampleFriendship;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockFriendshipRepository = Mockito.mock(FriendshipRepository.class);
        mockNotificationService = Mockito.mock(NotificationService.class);

        friendshipService = new FriendshipService(mockFriendshipRepository, mockNotificationService, null);

        sampleFriendship = new Friendship(1L, 2L, "PENDING");
        java.lang.reflect.Field idField = Friendship.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(sampleFriendship, 100L);
    }

    public void testSendRequest_Success() {
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(null);
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(2L, 1L)).thenReturn(null);
        when(mockFriendshipRepository.save(any(Friendship.class))).thenReturn(sampleFriendship);

        Friendship result = friendshipService.sendRequest(1L, 2L);

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        verify(mockNotificationService, times(1)).createFriendRequest(eq(1L), eq(2L), any());
    }

    public void testSendRequest_AlreadyExists() {
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(sampleFriendship);

        try {
            friendshipService.sendRequest(1L, 2L);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Friend request already exists", e.getMessage());
        }
    }

    public void testAcceptRequest_Success() {
        when(mockFriendshipRepository.findById(100L)).thenReturn(Optional.of(sampleFriendship));
        when(mockFriendshipRepository.save(any(Friendship.class))).thenReturn(sampleFriendship);

        Friendship result = friendshipService.acceptRequest(100L);

        assertNotNull(result);
        assertEquals("ACCEPTED", result.getStatus());
    }

    public void testGetPendingRequests() {
        when(mockFriendshipRepository.findByReceiverIdAndStatus(2L, "PENDING"))
                .thenReturn(Arrays.asList(sampleFriendship));

        List<Friendship> result = friendshipService.getPendingRequests(2L);

        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
    }

    public void testGetStatus_None() {
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(null);
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(2L, 1L)).thenReturn(null);

        String status = friendshipService.getStatus(1L, 2L);
        assertEquals("NONE", status);
    }

    public void testGetStatus_Friends() {
        sampleFriendship.setStatus("ACCEPTED");
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(sampleFriendship);

        String status = friendshipService.getStatus(1L, 2L);
        assertEquals("FRIENDS", status);
    }

    public void testGetStatus_Pending() {
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(null);
        when(mockFriendshipRepository.findBySenderIdAndReceiverId(2L, 1L)).thenReturn(sampleFriendship);

        String status = friendshipService.getStatus(1L, 2L);
        assertEquals("PENDING", status);
    }
}