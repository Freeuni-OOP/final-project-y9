package org.example.y9_gaming_site;


import junit.framework.TestCase;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserService;
import org.example.y9_gaming_site.user.UserRepository;
import org.mockito.Mockito;
import java.util.Optional;


public class UserRegisterTests extends TestCase {
    private UserRepository mockRepository;
    private UserService userService;
    private User validUser;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockRepository = Mockito.mock(UserRepository.class);
        userService = new UserService(mockRepository);
        validUser = new User();
        validUser.setUsername("mesatia");
        validUser.setEmail("mesatia@gmail.com");
        validUser.setPassword("mesatiaMagaria123");
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
        newUser.setPassword("laLalaLa1234");
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
        newUser.setPassword("laLalaLa1234");
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
}

