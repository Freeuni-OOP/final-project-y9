import org.example.y9_gaming_site.profile.UserProfileResponse;
import org.example.y9_gaming_site.profile.FileStorageService;
import org.example.y9_gaming_site.user.Role;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRegisterDto;
import org.example.y9_gaming_site.user.UserRepository;
import org.example.y9_gaming_site.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest{
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileStorageService fileStorageService;
    @InjectMocks
    private UserService userService;

    @Test
    void testSample1(){// get Profile should return Dto when User Exists
        User user = new User();
        user.setId(Long.valueOf(1L));
        user.setUsername("test");
        user.setAvatarUrl("avatar");
        when(userRepository.findById(Long.valueOf(1L))).thenReturn(Optional.of(user));

        UserProfileResponse profile = userService.getProfile(Long.valueOf(1L));

        assertThat(profile.getUsername()).isEqualTo("test");
        assertThat(profile.getAvatarUrl()).isEqualTo("avatar");
        assertThat(profile.getId()).isEqualTo(1L);
    }

    @Test
    void testSample2() {// correctly updates users avatar
        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        MultipartFile multipartFile = new MockMultipartFile("avatar", "test.jpg", "image/jpeg", "bytes".getBytes());
        when(fileStorageService.store(multipartFile)).thenReturn("/avatars/new-file.jpg");
        String res = userService.updateOrCreateAvatar(user.getUsername(), multipartFile);
        assertThat(res).isEqualTo("/avatars/new-file.jpg");
    }

    @Test
    public void testSample3() {//none existent user
        when(userRepository.findByUsername("none")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateOrCreateAvatar("none", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void testSample4(){ // getProfile
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void testSample5(){
        UserRegisterDto flagged = new UserRegisterDto();
        flagged.setUsername("hack");
        flagged.setEmail("hack@test.com");
        flagged.setPassword("Passw0rd!");
        flagged.setBirthDate(LocalDate.of(2000, 1, 1));
        assertThatThrownBy(() -> userService.addNewUser(flagged)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inappropriate");

        UserRegisterDto nullPassword = new UserRegisterDto();
        nullPassword.setUsername("CoolGamer99");
        nullPassword.setEmail("cool@test.com");
        nullPassword.setPassword(null);
        nullPassword.setBirthDate(LocalDate.of(2000, 1, 1));
        assertThatThrownBy(() -> userService.addNewUser(nullPassword))
                .hasMessageContaining("Password must be at least 8 characters");

        UserRegisterDto dupeEmail = new UserRegisterDto();
        dupeEmail.setUsername("CoolGamer99");
        dupeEmail.setEmail("taken@test.com");
        dupeEmail.setPassword("Passw0rd!");
        dupeEmail.setBirthDate(LocalDate.of(2000, 1, 1));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);
        assertThatThrownBy(() -> userService.addNewUser(dupeEmail)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        UserRegisterDto noBirthDate = new UserRegisterDto();
        noBirthDate.setUsername("CoolGamer99");
        noBirthDate.setEmail("cool2@test.com");
        noBirthDate.setPassword("Passw0rd!");
        noBirthDate.setBirthDate(null);
        assertThatThrownBy(() -> userService.addNewUser(noBirthDate)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Birth date");
    }

    @Test
    void testSample6() throws Exception { // addNewUser
        UserRegisterDto dto = new UserRegisterDto();
        dto.setUsername("CoolGamer99");
        dto.setEmail("cool3@test.com");
        dto.setPassword("Passw0rd!");
        dto.setBirthDate(LocalDate.of(2000, 1, 1));

        userService.addNewUser(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("CoolGamer99");
        assertThat(saved.getEmail()).isEqualTo("cool3@test.com");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.getBirthDate()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(saved.getSalt()).isNotBlank();
        assertThat(saved.getPassword()).isNotBlank();
    }

    @Test
    void testSample7(){ // createGuestUser builds and saves a guest account
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User guest = userService.createGuestUser();

        assertThat(guest.getUsername()).startsWith("Guest_");
        assertThat(guest.getEmail()).endsWith("@guest.y9gaming.local");
        assertThat(guest.getRole()).isEqualTo(Role.GUEST);
        assertThat(guest.getBirthDate()).isEqualTo(LocalDate.of(1970, 1, 1));
        assertThat(guest.getSalt()).isNotBlank();
        assertThat(guest.getPassword()).isNotBlank();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testSample8(){ // getProfileByUsername returns a dto when the user exists
        User user = new User();
        user.setId(5L);
        user.setUsername("CoolGamer99");
        user.setAvatarUrl("avatar.png");
        user.setRole(Role.USER);
        when(userRepository.findByUsername("CoolGamer99")).thenReturn(Optional.of(user));

        UserProfileResponse profile = userService.getProfileByUsername("CoolGamer99");

        assertThat(profile.getId()).isEqualTo(5L);
        assertThat(profile.getUsername()).isEqualTo("CoolGamer99");
        assertThat(profile.getAvatarUrl()).isEqualTo("avatar.png");
    }

    @Test
    void testSample9(){ // getProfileByUsername throws when the username doesn't exist
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfileByUsername("ghost"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void testSample10(){ // searchUsers maps every match to a profile dto
        User u1 = new User();
        u1.setId(1L); u1.setUsername("Alice"); u1.setRole(Role.USER);
        User u2 = new User();
        u2.setId(2L); u2.setUsername("Alicia"); u2.setRole(Role.ADMIN);
        when(userRepository.findByUsernameContainingIgnoreCase("ali")).thenReturn(List.of(u1, u2));

        List<UserProfileResponse> results = userService.searchUsers("ali");

        assertThat(results).hasSize(2);
        assertThat(results).extracting(UserProfileResponse::getUsername).containsExactly("Alice", "Alicia");
    }

    @Test
    void testSample11(){ // searchUsers returns an empty list when nothing matches
        when(userRepository.findByUsernameContainingIgnoreCase("zzz")).thenReturn(List.of());

        List<UserProfileResponse> results = userService.searchUsers("zzz");

        assertThat(results).isEmpty();
    }
}
