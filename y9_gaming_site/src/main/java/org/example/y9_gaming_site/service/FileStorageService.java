package org.example.y9_gaming_site.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
        return null;
    }

    private void validate(MultipartFile file){}

    private String extractExtension(String originFilename){
        return null;
    }

}
