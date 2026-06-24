package org.example.y9_gaming_site;

import jakarta.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import org.example.y9_gaming_site.auth.UserLoginController;
import org.example.y9_gaming_site.auth.UserLoginDto;
import org.example.y9_gaming_site.security.PasswordUtil;
import org.example.y9_gaming_site.security.TokenUtil;
import org.example.y9_gaming_site.streak.StreakService;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.example.y9_gaming_site.user.UserService;
import org.example.y9_gaming_site.user.Role;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.Optional;

public class UserAuthTests extends TestCase {

    private UserRepository mockRepository;
    private UserService mockUserService;
    private UserLoginController loginController;
    private User testUser;
    private String key;
    private String correctHashedPassword;
    private StreakService mockStreakService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockStreakService = Mockito.mock(StreakService.class);
        mockRepository = Mockito.mock(UserRepository.class);
        mockUserService = Mockito.mock(UserService.class);
        loginController = new UserLoginController(mockRepository, mockUserService, mockStreakService);
        key = PasswordUtil.generateKey();
        correctHashedPassword = PasswordUtil.hashPassword("mesatiasUyvarsGhomi<3", key);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("mst");
        testUser.setEmail("mst@gmail.com");
        testUser.setPassword(correctHashedPassword);
        testUser.setSalt(key);
        testUser.setRole(Role.USER);
    }

    public void test1() {
        Mockito.when(mockRepository.findByUsername("mst")).thenReturn(Optional.of(testUser));

        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("mst");
        loginDto.setPassword("mesatiasUyvarsGhomi<3");


        HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);

        ResponseEntity<?> response = loginController.login(loginDto, mockResponse);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body.get("token"));
        assertEquals("mst", body.get("username"));
        assertEquals("USER", body.get("role"));

        String token = (String) body.get("token");
        String validatedUser = TokenUtil.validateTokenAndGetUsername(token);
        assertEquals("mst", validatedUser);
    }

    public void test2() {
        Mockito.when(mockRepository.findByUsername("mst")).thenReturn(Optional.of(testUser));

        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("mst");
        loginDto.setPassword("mesatiasARuyvarsGhomi:(");


        HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);

        ResponseEntity<?> response = loginController.login(loginDto, mockResponse);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password.", response.getBody());
    }

    public void test3() {
        Mockito.when(mockRepository.findByUsername("stumariko")).thenReturn(Optional.empty());

        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("stumariko");
        loginDto.setPassword("rameRume123!");


        HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);

        ResponseEntity<?> response = loginController.login(loginDto, mockResponse);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password.", response.getBody());
    }

    public void test4() {
        String token = TokenUtil.generateToken("gamer1");
        assertNotNull(token);

        String tamperedToken = token + "badsignaturedata";
        String validatedUser = TokenUtil.validateTokenAndGetUsername(tamperedToken);

        assertNull(validatedUser);
    }

    public void test5() throws Exception {
        User guestUser = new User();
        guestUser.setId(99L);
        guestUser.setUsername("Guest_xyz789");
        guestUser.setRole(Role.GUEST);

        Mockito.when(mockUserService.createGuestUser()).thenReturn(guestUser);

        HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);


        ResponseEntity<?> response = loginController.joinAsGuest(mockResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue(((String) body.get("username")).startsWith("Guest_"));
        assertEquals("GUEST", body.get("role"));
        assertNotNull(body.get("token"));

        String token = (String) body.get("token");
        String validatedUser = TokenUtil.validateTokenAndGetUsername(token);
        assertEquals(guestUser.getUsername(), validatedUser);
    }
}