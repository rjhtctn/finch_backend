package com.rjhtctn.finch_backend.service;

import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.results.Result;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class ImageKitService {

    private final ImageKit imageKit;

    private static final Logger log = LoggerFactory.getLogger(ImageKitService.class);

    public ImageKitService(ImageKit imageKit) {
        this.imageKit = imageKit;
    }

    @Value("${imagekit.private-key}")
    private String privateKey;

    @Getter
    private String lastFileId;

    public String uploadImage(MultipartFile file, String folderName) {
        try {
            if (file == null || file.isEmpty())
                throw new IllegalArgumentException("File is empty or null");

            log.info("📤 Uploading image to ImageKit: {}", file.getOriginalFilename());

            String base64File = "data:" + file.getContentType() + ";base64," +
                    Base64.getEncoder().encodeToString(file.getBytes());

            FileCreateRequest request = new FileCreateRequest(base64File, file.getOriginalFilename());
            if (folderName != null && !folderName.isBlank()) {
                if (!folderName.startsWith("/")) folderName = "/" + folderName;
                request.setFolder(folderName);
            }

            request.setUseUniqueFileName(true);
            request.setPrivateFile(false);

            Result result = imageKit.upload(request);

            lastFileId = result.getFileId();

            log.info("✅ Upload success - fileId: {}, url: {}", result.getFileId(), result.getUrl());
            return result.getUrl();

        } catch (Exception e) {
            log.error("❌ ImageKit upload failed: {}", e.getMessage(), e);
            throw new RuntimeException("Image upload failed: " + e.getMessage(), e);
        }
    }

    public void deleteImage(String fileId) {
        try {
            if (fileId == null || fileId.isBlank()) {
                log.warn("⚠️ deleteImage: fileId boş, silme atlandı.");
                return;
            }

            imageKit.deleteFile(fileId);
            log.info("🗑️ Deleted ImageKit file: {}", fileId);

        } catch (Exception e) {
            log.error("❌ ImageKit delete failed: {}", e.getMessage(), e);
        }
    }

    public void deleteFolder(String folderPath) {
        try {
            if (folderPath == null || folderPath.isBlank())
                throw new IllegalArgumentException("Folder path cannot be empty");

            if (!folderPath.startsWith("/")) folderPath = "/" + folderPath;

            String apiUrl = "https://api.imagekit.io/v1/folder";
            String auth = privateKey + ":";
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            String jsonBody = "{\"folderPath\": \"" + folderPath + "\"}";
            byte[] postData = jsonBody.getBytes(StandardCharsets.UTF_8);

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("DELETE");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData);
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == 200 || responseCode == 204) {
                log.info("✅ Folder deleted successfully: {}", folderPath);
            } else if (responseCode == 404) {
                log.warn("⚠️ Folder not found: {}", folderPath);
            } else if (responseCode == 403) {
                log.warn("⚠️ Forbidden: Check permissions for {}", folderPath);
            } else {
                log.error("❌ Folder delete failed ({}) for {}", responseCode, folderPath);
            }

            conn.disconnect();

        } catch (Exception e) {
            log.error("❌ Folder deletion error: {}", e.getMessage(), e);
        }
    }
}