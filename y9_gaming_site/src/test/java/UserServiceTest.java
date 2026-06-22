import org.example.y9_gaming_site.dto.UserProfileResponse;
import org.example.y9_gaming_site.service.FileStorageService;
import org.example.y9_gaming_site.user.User;
import org.example.y9_gaming_site.user.UserRepository;
import org.example.y9_gaming_site.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        user.setId(Long.valueOf(1L));
        when(userRepository.findById(Long.valueOf(1L))).thenReturn(Optional.of(user));

        MultipartFile multipartFile = new MockMultipartFile("avatar", "test.jpg", "image/jpeg", "bytes".getBytes());
        when(fileStorageService.store(multipartFile)).thenReturn("/avatars/new-file.jpg");
        String res = userService.updateOrCreateAvatar(Long.valueOf(1L), multipartFile);
        assertThat(res).isEqualTo("/avatars/new-file.jpg");
    }

    @Test
    public void testSample3() {//none existent user
        when(userRepository.findById(Long.valueOf(10L))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateOrCreateAvatar(Long.valueOf(10L), null));
    }
}
