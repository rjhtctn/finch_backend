package com.rjhtctn.finch_backend.service;

import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.results.Result;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Base64;

@Service
public class ImageKitService {

    private final ImageKit imageKit;

    public ImageKitService(ImageKit imageKit) {
        this.imageKit = imageKit;
    }

    public String uploadImage(MultipartFile file, String folderName) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is empty or null");
            }

            System.out.println("===> Uploading to ImageKit: " + file.getOriginalFilename());

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

            System.out.println("✅ Upload success!");
            System.out.println("   File ID: " + result.getFileId());
            System.out.println("   File Path: " + result.getFilePath());
            System.out.println("   URL: " + result.getUrl());

            String imageUrl = result.getUrl();
            if (imageUrl == null || imageUrl.isBlank()) {
                String endpoint = imageKit.getConfig().getUrlEndpoint();
                if (endpoint == null || endpoint.isBlank()) {
                    throw new RuntimeException("ImageKit urlEndpoint missing in config!");
                }
                String filePath = result.getFilePath();
                if (filePath.startsWith("/")) filePath = filePath.substring(1);
                imageUrl = endpoint + filePath;
            }

            System.out.println("✅ Final Image URL: " + imageUrl);
            return imageUrl;

        } catch (Exception e) {
            System.out.println("❌ ImageKit upload failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Image upload failed: " + e.getMessage(), e);
        }
    }
}