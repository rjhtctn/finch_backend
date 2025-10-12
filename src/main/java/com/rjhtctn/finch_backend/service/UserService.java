package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.dto.request.UpdateUserProfileRequest;
import com.rjhtctn.finch_backend.dto.response.UserProfileResponse;
import com.rjhtctn.finch_backend.dto.response.UserResponse;
import com.rjhtctn.finch_backend.mapper.UserMapper;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public UserProfileResponse updateUserProfile(UUID userId, UpdateUserProfileRequest request) {
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        UserMapper.updateUserFromDto(userToUpdate, request);
        User updatedUser = userRepository.save(userToUpdate);
        return UserMapper.toUserProfileResponse(updatedUser);
    }

    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }
}