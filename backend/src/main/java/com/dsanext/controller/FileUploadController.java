package com.dsanext.controller;

import com.dsanext.domain.entity.User;
import com.dsanext.dto.common.ApiResponse;
import com.dsanext.dto.response.UserResponse;
import com.dsanext.exception.ValidationException;
import com.dsanext.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

/**
 * Handles profile image uploads.
 * Stores files on disk in the configured upload directory.
 * Returns the relative URL path for the stored image.
 *
 * POST /api/users/profile/image — Upload profile image
 */
@Slf4j
@RestController
@RequestMapping("/users/profile")
@RequiredArgsConstructor
public class FileUploadController {

    private final UserRepository userRepository;

    @Value("${dsanext.upload.profile-image-dir:./uploads/profiles}")
    private String uploadDir;

    private static final long     MAX_SIZE_BYTES  = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES =
            List.of("image/jpeg", "image/png", "image/webp");

    @PostMapping("/image")
    public ResponseEntity<ApiResponse<UserResponse>> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) throws IOException {

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new ValidationException("No file provided");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ValidationException("File size exceeds the 5MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new ValidationException("Only JPEG, PNG, or WebP images are allowed");
        }

        // Build unique filename: userId-uuid.ext
        String extension  = getExtension(file.getOriginalFilename(), contentType);
        String filename   = user.getId() + "-" + UUID.randomUUID() + "." + extension;
        Path   uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(filename);

        // Delete old profile image if it exists on disk
        deleteOldImage(user.getProfileImageUrl(), uploadPath);

        // Save new file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Update user record
        String imageUrl = "/uploads/profiles/" + filename;
        user.setProfileImageUrl(imageUrl);
        User saved = userRepository.save(user);

        log.info("Profile image uploaded for user {}: {}", user.getId(), filename);
        return ResponseEntity.ok(ApiResponse.success("Profile image updated", UserResponse.from(saved)));
    }

    private String getExtension(String originalName, String contentType) {
        if (originalName != null && originalName.contains(".")) {
            String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
            if (List.of("jpg","jpeg","png","webp").contains(ext)) return ext;
        }
        return switch (contentType) {
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            default           -> "jpg";
        };
    }

    private void deleteOldImage(String oldUrl, Path uploadPath) {
        if (oldUrl == null || !oldUrl.startsWith("/uploads/profiles/")) return;
        try {
            String oldFilename = oldUrl.substring(oldUrl.lastIndexOf('/') + 1);
            Path   oldPath     = uploadPath.resolve(oldFilename);
            Files.deleteIfExists(oldPath);
        } catch (IOException ex) {
            log.warn("Could not delete old profile image: {}", ex.getMessage());
        }
    }
}
