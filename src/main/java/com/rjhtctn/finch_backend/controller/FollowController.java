package com.rjhtctn.finch_backend.controller;

import com.rjhtctn.finch_backend.dto.follow.FollowRequestDto;
import com.rjhtctn.finch_backend.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping
    public ResponseEntity<String> followUser(
            @RequestBody FollowRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String result = followService.followUser(request.getUsername(), userDetails);

        if ("requested".equals(result)) {
            return ResponseEntity.ok("Follow request sent to private user: " + request.getUsername());
        }

        return ResponseEntity.ok("Successfully followed user: " + request.getUsername());
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<String> unfollowUser(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails userDetails) {

        followService.unfollowUser(username, userDetails);
        return ResponseEntity.ok("Successfully unfollowed user: " + username);
    }

    @DeleteMapping("/remove-follower/{username}")
    public ResponseEntity<String> removeFollower(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails userDetails) {

        followService.removeFollower(userDetails, username);
        return ResponseEntity.ok("Removed " + username + " from your followers.");
    }
}