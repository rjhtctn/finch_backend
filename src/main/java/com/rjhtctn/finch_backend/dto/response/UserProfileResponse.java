package com.rjhtctn.finch_backend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserProfileResponse {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private String bio;
    private String location;
    private String website;
    private String profileImageUrl;
    private String bannerImageUrl;
    private LocalDateTime createdAt;
}