package com.rjhtctn.finch_backend.mapper;

import com.rjhtctn.finch_backend.dto.user.UpdateUserProfileRequest;
import com.rjhtctn.finch_backend.dto.user.UserMeResponse;
import com.rjhtctn.finch_backend.dto.user.UserProfileResponse;
import com.rjhtctn.finch_backend.dto.user.UserResponse;
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
        dto.setProfileImageUrl(user.getProfileImageUrl());

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
        if (userToUpdate == null) {
            return;
        }

        if (request == null) {
            return;
        }

        userToUpdate.setFirstName(request.getFirstName());
        userToUpdate.setLastName(request.getLastName());
        userToUpdate.setBio(request.getBio());
        userToUpdate.setLocation(request.getLocation());
        userToUpdate.setWebsite(request.getWebsite());
        userToUpdate.setProfileImageUrl(request.getProfileImageUrl());
        userToUpdate.setBannerImageUrl(request.getBannerImageUrl());
    }

    public static UserMeResponse toUserMeResponse(User user) {
        if (user == null) {
            return null;
        }

        UserMeResponse dto = new UserMeResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setBio(user.getBio());
        dto.setLocation(user.getLocation());
        dto.setWebsite(user.getWebsite());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setBannerImageUrl(user.getBannerImageUrl());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        return dto;

    }
}