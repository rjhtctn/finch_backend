package com.rjhtctn.finch_backend.controller;

import com.rjhtctn.finch_backend.dto.follow.FollowRequestResponseDto;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.service.FollowRequestService;
import com.rjhtctn.finch_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/follow-request")
public class FollowRequestController {

    private final FollowRequestService followRequestService;
    private final UserService userService;

    public FollowRequestController(FollowRequestService followRequestService, UserService userService) {
        this.followRequestService = followRequestService;
        this.userService = userService;
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<String> accept(@PathVariable UUID requestId) {
        followRequestService.acceptRequest(requestId);
        return ResponseEntity.ok("Follow request accepted.");
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<String> reject(@PathVariable UUID requestId) {
        followRequestService.rejectRequest(requestId);
        return ResponseEntity.ok("Follow request rejected.");
    }

    @GetMapping("/pending")
    public ResponseEntity<List<FollowRequestResponseDto>> getPendingRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        User receiver = userService.findUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(followRequestService.getPendingRequests(receiver));
    }
}