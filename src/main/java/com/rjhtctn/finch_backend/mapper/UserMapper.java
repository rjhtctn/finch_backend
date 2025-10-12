package com.rjhtctn.finch_backend.mapper;

import com.rjhtctn.finch_backend.dto.request.UpdateUserProfileRequest;
import com.rjhtctn.finch_backend.dto.response.UserProfileResponse;
import com.rjhtctn.finch_backend.dto.response.UserResponse;
import com.rjhtctn.finch_backend.model.User;

public class UserMapper {

    public static UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());

        return dto;
    }

    public static UserProfileResponse toUserProfileResponse(User user) {
        if (user == null) {
            return null;
        }

        UserProfileResponse dto = new UserProfileResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setBio(user.getBio());
        dto.setLocation(user.getLocation());
        dto.setWebsite(user.getWebsite());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setBannerImageUrl(user.getBannerImageUrl());
        dto.setCreatedAt(user.getCreatedAt());

        return dto;
    }

    public static void updateUserFromDto(User userToUpdate, UpdateUserProfileRequest request) {
        userToUpdate.setFirstName(request.getFirstName());
        userToUpdate.setLastName(request.getLastName());
        userToUpdate.setBio(request.getBio());
        userToUpdate.setLocation(request.getLocation());
        userToUpdate.setWebsite(request.getWebsite());
        userToUpdate.setProfileImageUrl(request.getProfileImageUrl());
        userToUpdate.setBannerImageUrl(request.getBannerImageUrl());
    }
}