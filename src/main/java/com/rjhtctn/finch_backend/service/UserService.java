package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.dto.finch.FinchResponse;
import com.rjhtctn.finch_backend.dto.user.UpdateUserProfileRequest;
import com.rjhtctn.finch_backend.dto.user.UserMeResponse;
import com.rjhtctn.finch_backend.dto.user.UserProfileResponse;
import com.rjhtctn.finch_backend.dto.user.UserResponse;
import com.rjhtctn.finch_backend.mapper.UserMapper;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FinchService finchService;


    public UserService(UserRepository userRepository, FinchService finchService) {
        this.userRepository = userRepository;
        this.finchService = finchService;
    }

    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return UserMapper.toUserProfileResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    public UserProfileResponse updateUserProfile(String username, UpdateUserProfileRequest request) {
        User userToUpdate = findUserByUsername(username);

        UserMapper.updateUserFromDto(userToUpdate, request);
        User updatedUser = userRepository.save(userToUpdate);
        return UserMapper.toUserProfileResponse(updatedUser);
    }

    public void deleteUser(String username) {
        User userToDelete = findUserByUsername(username);

        userRepository.delete(userToDelete);
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    public UserMeResponse getMyProfile(String username) {
        User user = findUserByUsername(username);

        return UserMapper.toUserMeResponse(user);
    }

    public List<FinchResponse> getFinchesOfUser(String username) {
        findUserByUsername(username);

        if (findUserByUsername(username) == null) {
            throw new RuntimeException("User not found with username: " + username);
        }

        return finchService.getFinchesByUsername(username);
    }
}