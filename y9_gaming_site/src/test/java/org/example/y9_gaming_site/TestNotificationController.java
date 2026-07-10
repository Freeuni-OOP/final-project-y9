package org.example.y9_gaming_site;

import junit.framework.TestCase;
import org.example.y9_gaming_site.notification.Notification;
import org.example.y9_gaming_site.notification.NotificationController;
import org.example.y9_gaming_site.notification.NotificationService;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TestNotificationController extends TestCase {

    private NotificationService mockNotificationService;
    private MockMvc mockMvc;
    private Notification sampleNotification;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockNotificationService = Mockito.mock(NotificationService.class);

        NotificationController controller = new NotificationController(mockNotificationService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        sampleNotification = new Notification(2L, 1L, "FRIEND_REQUEST", "lasha sent you a friend request", 50L);
        sampleNotification.setId(10L);
    }

    public void test1() throws Exception {
        when(mockNotificationService.getNotifications(2L)).thenReturn(Arrays.asList(sampleNotification));

        mockMvc.perform(get("/notifications/user/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].type").value("FRIEND_REQUEST"));
    }

    public void test2() throws Exception {
        when(mockNotificationService.getUnreadCount(2L)).thenReturn(3);

        mockMvc.perform(get("/notifications/unread-count/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    public void test3() throws Exception {
        doNothing().when(mockNotificationService).acceptFriendship(10L);

        mockMvc.perform(post("/notifications/accept/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Friendship accepted"));
    }

    public void test4() throws Exception {
        doNothing().when(mockNotificationService).declineFriendship(10L);

        mockMvc.perform(post("/notifications/decline/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Friendship declined"));
    }

    public void test5() throws Exception {
        doNothing().when(mockNotificationService).markRead(2L);

        mockMvc.perform(post("/notifications/mark-read/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Seen"));
    }
}