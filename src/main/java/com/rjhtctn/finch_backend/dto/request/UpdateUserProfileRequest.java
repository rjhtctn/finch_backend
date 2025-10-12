package com.rjhtctn.finch_backend.dto.request;

import lombok.Data;

@Data
public class UpdateUserProfileRequest {
    private String firstName;
    private String lastName;
    private String bio;
    private String location;
    private String website;
    private String profileImageUrl;
    private String bannerImageUrl;
}