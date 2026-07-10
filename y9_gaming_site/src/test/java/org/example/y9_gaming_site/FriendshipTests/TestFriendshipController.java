package org.example.y9_gaming_site.FriendshipTests;

import junit.framework.TestCase;
import org.example.y9_gaming_site.friendship.Friendship;
import org.example.y9_gaming_site.friendship.FriendshipController;
import org.example.y9_gaming_site.friendship.FriendshipService;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TestFriendshipController extends TestCase {

    private FriendshipService mockFriendshipService;
    private MockMvc mockMvc;
    private Friendship sampleFriendship;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockFriendshipService = Mockito.mock(FriendshipService.class);

        FriendshipController controller = new FriendshipController(mockFriendshipService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        sampleFriendship = new Friendship(1L, 2L, "PENDING");
    }


    public void test1() throws Exception {
        when(mockFriendshipService.sendRequest(1L, 2L)).thenReturn(sampleFriendship);

        String jsonPayload = "{\"senderId\":1,\"receiverId\":2}";

        mockMvc.perform(post("/friends/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }


    public void test2() throws Exception {
        sampleFriendship.setStatus("ACCEPTED");
        when(mockFriendshipService.acceptRequest(100L)).thenReturn(sampleFriendship);

        String jsonPayload = "{\"friendshipId\":100}";

        mockMvc.perform(post("/friends/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }


    public void test3() throws Exception {
        when(mockFriendshipService.getPendingRequests(1L)).thenReturn(Arrays.asList(sampleFriendship));

        mockMvc.perform(get("/friends/pending/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }


    public void test4() throws Exception {
        when(mockFriendshipService.getStatus(1L, 2L)).thenReturn("FRIENDS");

        mockMvc.perform(get("/friends/status")
                        .param("myId", "1")
                        .param("otherId", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("FRIENDS"));
    }
}