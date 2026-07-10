import org.example.y9_gaming_site.profile.FileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class FileStorageServiceTest {
    private final FileStorageService fileStorageService = new FileStorageService("test-uploads", "/avatars");

    @AfterEach
    void cleanIt() throws IOException {
        Path path = Paths.get("test-uploads");
        if (Files.exists(path)) {
            try (Stream<Path> stream = Files.walk(path)) {
                stream.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
    }

    @Test
    void store_savesFileAndReturnsUrl() {
        MultipartFile file = new MockMultipartFile("avatar", "pic.png", "image/png", "bytes".getBytes());

        String url = fileStorageService.store(file);

        assertThat(url).startsWith("/avatars/");
        assertThat(url).endsWith(".png");
    }

    @Test
    void store_actuallyWritesTheFileToDisk() throws IOException {
        byte[] content = "pretend-image-bytes".getBytes();
        MultipartFile file = new MockMultipartFile("avatar", "pic.png", "image/png", content);

        String url = fileStorageService.store(file);

        String fileName = url.substring(url.lastIndexOf('/') + 1);
        Path written = Path.of("test-uploads").toAbsolutePath().normalize().resolve(fileName);
        assertThat(Files.exists(written)).isTrue();
        assertThat(Files.readAllBytes(written)).isEqualTo(content);
    }

    @Test
    void store_throws_whenFileIsEmpty() {
        MultipartFile file = new MockMultipartFile("avatar", "pic.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> fileStorageService.store(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void store_throws_whenFileIsNull() {
        assertThatThrownBy(() -> fileStorageService.store(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void store_throws_whenContentTypeIsNotAllowed() {
        MultipartFile file = new MockMultipartFile("avatar", "doc.pdf", "application/pdf", "bytes".getBytes());

        assertThatThrownBy(() -> fileStorageService.store(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PNG, JPEG or WEBP");
    }

    @Test
    void store_keepsUrlExtensionless_whenOriginalFilenameHasNoExtension() {
        MultipartFile file = new MockMultipartFile("avatar", "pic", "image/png", "bytes".getBytes());

        String url = fileStorageService.store(file);

        assertThat(url).startsWith("/avatars/");
        assertThat(url).doesNotContain(".");
    }

}
