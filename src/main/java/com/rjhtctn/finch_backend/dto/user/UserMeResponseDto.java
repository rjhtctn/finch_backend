package com.rjhtctn.finch_backend.dto.user;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserMeResponseDto {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String bio;
    private String location;
    private String website;
    private String profileImageUrl;
    private String bannerImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isPrivate;
}