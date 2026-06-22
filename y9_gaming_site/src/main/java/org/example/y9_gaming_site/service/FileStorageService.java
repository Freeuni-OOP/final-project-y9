package org.example.y9_gaming_site.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final List<String> ALLOWED_TYPES = List.of("image/png", "image.jepg", "image/webp");
    private final String uploadDir;
    private final String urlPrefix;

    public FileStorageService(@Value("${app.upload.avatar-dir:uploads/avatars}") String uploadDir,
                              @Value("${app.upload.avatar-url-prefix:/avatars}") String urlPrefix){
        this.uploadDir = uploadDir;
        this.urlPrefix = urlPrefix;
    }

    public String store(MultipartFile file){
        validate(file);
        try {
            Path uploadPath = Path.of(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String fileNaem = UUID.randomUUID()+ extractExtension(file.getOriginalFilename());
            Path targetPath = uploadPath.resolve(fileNaem);

            try (InputStream inputStream = file.getInputStream()){
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return urlPrefix +"/" +fileNaem;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void validate(MultipartFile file){
        if(file == null||file.isEmpty()){
            throw new IllegalArgumentException();
        }
        String contentType = file.getContentType();
        if(!ALLOWED_TYPES.contains(contentType)){
            throw new IllegalArgumentException();
        }
    }

    private String extractExtension(String originFilename) {
        if(originFilename == null || !originFilename.contains(".")){
            return "";
        }
        return originFilename.substring(originFilename.lastIndexOf("."));

    }

}
