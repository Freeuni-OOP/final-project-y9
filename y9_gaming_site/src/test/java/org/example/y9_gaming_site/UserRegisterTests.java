package org.example.y9_gaming_site;


import junit.framework.TestCase;
import org.example.y9_gaming_site.security.ContentModerator;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserService;
import org.example.y9_gaming_site.user.UserRepository;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;


public class UserRegisterTests extends TestCase {
    private UserRepository mockRepository;
    private UserService userService;
    private User validUser;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockRepository = Mockito.mock(UserRepository.class);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        userService = new UserService(mockRepository, encoder);
        validUser = new User();
        validUser.setUsername("mesatia");
        validUser.setEmail("mesatia@gmail.com");
        validUser.setPassword("mesatiaMagaria123!");
    }

    public void test0(){
        validUser.setId(4L);
        assertTrue(validUser.getId()==4L);
    }

    public void test1() throws Exception {
        Mockito.when(mockRepository.findByUsername("mesatia")).thenReturn(Optional.empty());
        Mockito.when(mockRepository.save(validUser)).thenReturn(validUser);
        User savedUser = userService.addNewUser(validUser);
        assertNotNull(savedUser);
        assertEquals("mesatia", savedUser.getUsername());
    }


    public void test2() {
        validUser.setPassword("123");
        try {
            userService.addNewUser(validUser);
            fail("Should have thrown an exception for a weak password!");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Password must be at least 8 characters long"));
        }
    }

    public void test3(){
        User existingUser = new User();
        existingUser.setUsername("mesatia");
        Mockito.when(mockRepository.findByUsername("mesatia")).thenReturn(Optional.of(existingUser));
        User newUser = new User();
        newUser.setUsername("mesatia");
        newUser.setPassword("laLalaLa1234#");
        newUser.setEmail("mesat23@freeuni.edu.ge");
        try {
            userService.addNewUser(newUser);
            fail("Should have thrown an exception for a used username");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("This Username is already taken"));
        }
    }

    public void test4() {
        User existingUser = new User();
        existingUser.setUsername("mesatia");
        Mockito.when(mockRepository.findByUsername("mesatia")).thenReturn(Optional.of(existingUser));
        Mockito.when(mockRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(mockRepository.findByUsername("mesatia")).thenReturn(Optional.of(existingUser));
        User newUser = new User();
        newUser.setUsername("mesatia");
        newUser.setPassword("laLalaLa1234#");
        newUser.setEmail("test@freeuni.edu.ge");
        try {
            userService.addNewUser(newUser);
            fail("Should have thrown an exception for a taken username");
        } catch (Exception e) {
            String message = e.getMessage();
            assertTrue(message.contains("This Username is already taken, you can try one of these: "));
            String suggestionsPart = message.substring(message.indexOf("these: ") + 7);
            String[] suggestions = suggestionsPart.split(", ");
            assertEquals("Should provide exactly 3 suggestions", 3, suggestions.length);
            for (String suggestion : suggestions) {
                assertNotNull(suggestion);
                assertTrue("Suggestion should be dynamic", suggestion.length() > "mesatia".length());
                boolean matchesPattern = suggestion.matches("mesatia\\d+") ||
                        suggestion.matches("mesatia_\\d+") ||
                        suggestion.matches("(Epic|Shadow|Cyber|Cosmic|Ghost|TheReal|Amazing|TheOneAndOnly)mesatia");

                assertTrue("Suggestion '" + suggestion + "' did not match any expected generation style", matchesPattern);
            }
        }
    }

    public void test5() throws Exception {
        Mockito.when(mockRepository.findByUsername("uniqueUser")).thenReturn(Optional.empty());
        Mockito.when(mockRepository.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        User user = new User();
        user.setUsername("coolerMesatia");
        user.setEmail("meeesaaatiaaaa@gmail.com");
        user.setPassword("dzalianMagariParoli123!");
        User savedUser = userService.addNewUser(user);
        assertNotNull(savedUser);
        assertFalse(savedUser.getPassword().equals("dzalianMagariParoli123!"));
        assertTrue(savedUser.getPassword().startsWith("$2a$"));
    }

    public void test6() {
        String testUser = "mesatia";
        String token = org.example.y9_gaming_site.security.TokenUtil.generateToken(testUser);
        assertNotNull("Token should not be null", token);
        assertTrue("Token should contain a dot separator", token.contains("."));
        String extractedUser = org.example.y9_gaming_site.security.TokenUtil.validateTokenAndGetUsername(token);
        assertEquals("Extracted username should match the original", testUser, extractedUser);
        String tamperedToken = token + "fakeBytes";
        String invalidUser = org.example.y9_gaming_site.security.TokenUtil.validateTokenAndGetUsername(tamperedToken);
        assertNull("A tampered token must return null user authentication", invalidUser);
    }

    public void test7(){
        assertFalse(ContentModerator.isFlagged("mesatia"));
        assertFalse(ContentModerator.isFlagged("pixelart_gamer"));
    }


    public void test8(){
        assertTrue(ContentModerator.isFlagged("admin"));
        assertTrue(ContentModerator.isFlagged("h4ck"));
    }


    public void test9(){
        assertTrue(ContentModerator.isFlagged("h4ck"));
        assertTrue(ContentModerator.isFlagged("xX_h4ck_Xx"));
    }

    public void test10(){
        assertTrue(ContentModerator.isFlagged("hhhaaaaccckkkkkkk"));
        assertTrue(ContentModerator.isFlagged("adm||nnnnnnnnn"));
    }

}

