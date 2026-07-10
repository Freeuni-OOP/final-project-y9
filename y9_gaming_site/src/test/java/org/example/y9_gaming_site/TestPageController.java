package org.example.y9_gaming_site;

import junit.framework.TestCase;
import org.example.y9_gaming_site.chat.PageController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class TestPageController extends TestCase {

    private MockMvc mockMvc;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        PageController pageController = new PageController();
        this.mockMvc = MockMvcBuilders.standaloneSetup(pageController).build();
    }

    public void test1() throws Exception {
        mockMvc.perform(get("/chat"))
                .andExpect(status().isOk())
                .andExpect(view().name("Chat"));
    }
}