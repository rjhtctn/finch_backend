package com.rjhtctn.finch_backend.controller;

import com.rjhtctn.finch_backend.dto.finch.FinchResponse;
import com.rjhtctn.finch_backend.dto.user.UpdateUserProfileRequest;
import com.rjhtctn.finch_backend.dto.user.UserMeResponse;
import com.rjhtctn.finch_backend.dto.user.UserProfileResponse;
import com.rjhtctn.finch_backend.dto.user.UserResponse;
import com.rjhtctn.finch_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getUserProfileByUsername(@PathVariable String username) {
        UserProfileResponse userProfile = userService.getOneUser(username);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/me")
    public ResponseEntity<UserMeResponse> updateUserProfile(
            @RequestBody UpdateUserProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserMeResponse updatedUser = userService.updateUserProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(userDetails);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserMeResponse myProfile = userService.getMyProfile(userDetails);

        return ResponseEntity.ok(myProfile);
    }

    @GetMapping("/{username}/finch")
    public ResponseEntity<List<FinchResponse>> getFinchesOfUser(@PathVariable String username) {
        List<FinchResponse> finches = userService.getFinchesOfUser(username);
        return ResponseEntity.ok(finches);
    }

    @GetMapping("/{username}/followers")
    public ResponseEntity<List<UserResponse>> getFollowers(@PathVariable String username) {
        return ResponseEntity.ok(userService.getFollowers(username));
    }

    @GetMapping("/{username}/following")
    public ResponseEntity<List<UserResponse>> getFollowing(@PathVariable String username) {
        return ResponseEntity.ok(userService.getFollowing(username));
    }

    @GetMapping("/me/followers")
    public ResponseEntity<List<UserResponse>> getMyFollowers(@AuthenticationPrincipal UserDetails userDetails) {
        List<UserResponse> followers = userService.getMyFollowers(userDetails);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/me/following")
    public ResponseEntity<List<UserResponse>> getMyFollowing(@AuthenticationPrincipal UserDetails userDetails) {
        List<UserResponse> following = userService.getMyFollowing(userDetails);
        return ResponseEntity.ok(following);
    }

    @GetMapping("/me/liked_finches")
    public ResponseEntity<List<FinchResponse>> getLikedFinches(@AuthenticationPrincipal UserDetails userDetails) {
        List<FinchResponse> likedFinches = userService.getMyLikedFinches(userDetails);
        return ResponseEntity.ok(likedFinches);
    }
}