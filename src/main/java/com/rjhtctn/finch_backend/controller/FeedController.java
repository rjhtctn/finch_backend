package com.rjhtctn.finch_backend.controller;

import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.service.FeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/global")
    public ResponseEntity<List<FinchResponseDto>> getGlobalFeed(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(feedService.getGlobalFeed(userDetails));
    }

    @GetMapping("/following")
    public ResponseEntity<List<FinchResponseDto>> getFollowingFeed(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(feedService.getFollowingFeed(userDetails));
    }
}