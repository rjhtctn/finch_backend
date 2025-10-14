package com.rjhtctn.finch_backend.mapper;

import com.rjhtctn.finch_backend.dto.user.UpdateUserProfileRequestDto;
import com.rjhtctn.finch_backend.dto.user.UserMeResponseDto;
import com.rjhtctn.finch_backend.dto.user.UserProfileResponseDto;
import com.rjhtctn.finch_backend.dto.user.UserResponseDto;
import com.rjhtctn.finch_backend.model.User;

public class UserMapper {

    public static UserResponseDto toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setProfileImageUrl(user.getProfileImageUrl());

        return dto;
    }

    public static UserProfileResponseDto toUserProfileResponse(User user) {
        if (user == null) {
            return null;
        }

        UserProfileResponseDto dto = new UserProfileResponseDto();
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

    public static void updateUserFromDto(User userToUpdate, UpdateUserProfileRequestDto request) {
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

    public static UserMeResponseDto toUserMeResponse(User user) {
        if (user == null) {
            return null;
        }

        UserMeResponseDto dto = new UserMeResponseDto();
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