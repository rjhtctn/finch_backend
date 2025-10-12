package com.rjhtctn.finch_backend.controller;

import com.rjhtctn.finch_backend.dto.request.UpdateUserProfileRequest;
import com.rjhtctn.finch_backend.dto.response.UserProfileResponse;
import com.rjhtctn.finch_backend.dto.response.UserResponse;
import com.rjhtctn.finch_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getUserProfileByUsername(@PathVariable String username) {
        UserProfileResponse userProfile = userService.getUserProfile(username);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @PathVariable UUID userId,
            @RequestBody UpdateUserProfileRequest request) {

        UserProfileResponse updatedUser = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }
}