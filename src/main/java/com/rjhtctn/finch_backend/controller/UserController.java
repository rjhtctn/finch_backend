package com.rjhtctn.finch_backend.controller;

import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.dto.user.*;
import com.rjhtctn.finch_backend.service.UserService;
import jakarta.validation.Valid;
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
    public ResponseEntity<UserProfileResponseDto> getUserProfileByUsername(@PathVariable String username){
        UserProfileResponseDto userProfile = userService.getOneUser(username);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/me")
    public ResponseEntity<UserMeResponseDto> updateUserProfile(
            @RequestBody UpdateUserProfileRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserMeResponseDto updatedUser = userService.updateUserProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(userDetails);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponseDto> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserMeResponseDto myProfile = userService.getMyProfile(userDetails);

        return ResponseEntity.ok(myProfile);
    }

    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequestDto request) {

        userService.changePassword(userDetails, request);
        return ResponseEntity.ok("Password changed successfully.");
    }

    @GetMapping("/me/finch")
    public ResponseEntity<List<FinchResponseDto>> getMyFinches(@AuthenticationPrincipal UserDetails userDetails){
        List <FinchResponseDto> myFinches = userService.getMyFinches(userDetails);
        return ResponseEntity.ok(myFinches);
    }

    @GetMapping("/{username}/finch")
    public ResponseEntity<List<FinchResponseDto>> getFinchesOfUser(@PathVariable String username,
                                                                   @AuthenticationPrincipal UserDetails userDetails) {
        List<FinchResponseDto> finches = userService.getFinchesOfUser(username, userDetails);
        return ResponseEntity.ok(finches);
    }

    @GetMapping("/{username}/followers")
    public ResponseEntity<List<UserResponseDto>> getFollowers(@PathVariable String username,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getFollowers(username, userDetails));
    }

    @GetMapping("/{username}/following")
    public ResponseEntity<List<UserResponseDto>> getFollowing(@PathVariable String username,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getFollowing(username, userDetails));
    }

    @GetMapping("/me/followers")
    public ResponseEntity<List<UserResponseDto>> getMyFollowers(@AuthenticationPrincipal UserDetails userDetails) {
        List<UserResponseDto> followers = userService.getMyFollowers(userDetails);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/me/following")
    public ResponseEntity<List<UserResponseDto>> getMyFollowing(@AuthenticationPrincipal UserDetails userDetails) {
        List<UserResponseDto> following = userService.getMyFollowing(userDetails);
        return ResponseEntity.ok(following);
    }

    @GetMapping("/me/liked_finches")
    public ResponseEntity<List<FinchResponseDto>> getLikedFinches(@AuthenticationPrincipal UserDetails userDetails) {
        List<FinchResponseDto> likedFinches = userService.getMyLikedFinches(userDetails);
        return ResponseEntity.ok(likedFinches);
    }

    @PostMapping("/set-private")
    public ResponseEntity<String> setPrivate(@AuthenticationPrincipal UserDetails userDetails) {
        userService.setPrivateUser(userDetails);
        return ResponseEntity.ok("Private user has been set successfully.");
    }

    @PostMapping("/set-public")
    public ResponseEntity<String> setPublic(@AuthenticationPrincipal UserDetails userDetails) {
        userService.setPublicUser(userDetails);
        return ResponseEntity.ok("Public user has been set successfully.");
    }
}