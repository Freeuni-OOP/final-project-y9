package org.example.y9_gaming_site.ChatTests;

import junit.framework.TestCase;
import org.example.y9_gaming_site.chat.*;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TestChatController extends TestCase {

    private ChatService mockChatService;
    private MockMvc mockMvc;
    private Message sampleMessage;
    private ChatRoom sampleRoom;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mockChatService = Mockito.mock(ChatService.class);

        ChatController chatController = new ChatController(mockChatService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();

        sampleMessage = new Message();
        sampleMessage.setId(100L);
        sampleMessage.setSenderId(1L);
        sampleMessage.setRoomId(10L);
        sampleMessage.setMessage("Hello testing!");

        sampleRoom = new ChatRoom(null, "PRIVATE");
        sampleRoom.setId(5L);
    }


    public void test1() throws Exception {
        when(mockChatService.messageSender(1L, 10L, "Hello testing!")).thenReturn(sampleMessage);


        String jsonPayload = "{\"senderId\":1,\"roomId\":10,\"message\":\"Hello testing!\"}";

        mockMvc.perform(post("/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.message").value("Hello testing!"));
    }

    public void test2() throws Exception {
        Map<String, Object> mockMsgMap = new HashMap<>();
        mockMsgMap.put("id", 100L);
        mockMsgMap.put("senderUsername", "lasha");
        mockMsgMap.put("message", "Hello testing!");

        when(mockChatService.getMessages(10L)).thenReturn(Arrays.asList(mockMsgMap));

        mockMvc.perform(get("/chat/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].senderUsername").value("lasha"));
    }

    public void test3() throws Exception {
        when(mockChatService.openPrivateRoom(1L, 2L)).thenReturn(sampleRoom);

        mockMvc.perform(post("/chat/open-private/1/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.type").value("PRIVATE"));
    }


    public void test4() throws Exception {
        ChatRoom groupRoom = new ChatRoom("Gamer Lobbies", "GROUP");
        groupRoom.setId(7L);

        when(mockChatService.createGroupRoom(eq("Gamer Lobbies"), eq("GROUP"), any(List.class)))
                .thenReturn(groupRoom);


        String jsonPayload = "[1,2,3]";

        mockMvc.perform(post("/chat/create-group")
                        .param("name", "Gamer Lobbies")
                        .param("type", "GROUP")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Gamer Lobbies"));
    }

    public void test5() throws Exception {
        Map<String, Object> mockUserMap = new HashMap<>();
        mockUserMap.put("id", 1L);
        mockUserMap.put("username", "gio");

        when(mockChatService.findUserByUsername("gio")).thenReturn(mockUserMap);

        mockMvc.perform(get("/chat/find-user/gio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("gio"));
    }
}