package org.example.y9_gaming_site;

import junit.framework.TestCase;
import org.example.y9_gaming_site.security.ContentModerator;
import org.example.y9_gaming_site.security.TokenUtil;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserService;
import org.example.y9_gaming_site.user.UserRepository;
import org.example.y9_gaming_site.user.UserRegisterDto;
import org.mockito.Mockito;

import java.time.LocalDate;

public class UserRegisterTests extends TestCase {
    private UserRepository mockRepository;
    private UserService userService;
    private UserRegisterDto validUserDto;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockRepository = Mockito.mock(UserRepository.class);
        userService = new UserService(mockRepository);

        validUserDto = new UserRegisterDto();
        validUserDto.setUsername("mesatia");
        validUserDto.setEmail("mesatia@gmail.com");
        validUserDto.setPassword("mesatiaMagaria123!");
        validUserDto.setBirthDate(LocalDate.parse("2005-02-12"));
    }

    public void test0(){
        User user = new User();
        user.setId(4L);
        assertTrue(user.getId() == 4L);
    }

    public void test1() throws Exception {
        Mockito.when(mockRepository.existsByUsername("mesatia")).thenReturn(false);
        Mockito.when(mockRepository.existsByEmail("mesatia@gmail.com")).thenReturn(false);

        try {
            userService.addNewUser(validUserDto);
        } catch (Exception e) {
            fail("Should not have thrown any exception for valid data");
        }

        Mockito.verify(mockRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }

    public void test2() {
        validUserDto.setPassword("123");
        try {
            userService.addNewUser(validUserDto);
            fail("Should have thrown an exception for a weak password!");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Password must be at least 8 characters long"));
        }
    }

    public void test3(){
        Mockito.when(mockRepository.existsByUsername("mesatia")).thenReturn(true);

        UserRegisterDto newUser = new UserRegisterDto();
        newUser.setUsername("mesatia");
        newUser.setPassword("laLalaLa1234#");
        newUser.setEmail("mesat23@freeuni.edu.ge");

        try {
            userService.addNewUser(newUser);
            fail("Should have thrown an exception for a used username");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Username is already taken"));
        }
    }

    public void test4() {
        Mockito.when(mockRepository.existsByUsername("mesatia")).thenReturn(true);
        Mockito.when(mockRepository.existsByUsername(Mockito.anyString())).thenReturn(false);
        Mockito.when(mockRepository.existsByUsername("mesatia")).thenReturn(true);
        UserRegisterDto newUser = new UserRegisterDto();
        newUser.setUsername("mesatia");
        newUser.setPassword("laLalaLa1234#");
        newUser.setEmail("test@freeuni.edu.ge");

        try {
            userService.addNewUser(newUser);
            fail("Should have thrown an exception for a taken username");
        } catch (Exception e) {
            String message = e.getMessage();
            assertTrue(message.contains("Similar available Options: "));
            String suggestionsPart = message.substring(message.indexOf("Options: ") + 9);
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