package com.rjhtctn.finch_backend.controller;

import com.rjhtctn.finch_backend.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/finch/{finchId}/likes")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping
    public ResponseEntity<String> likeFinch(
            @PathVariable UUID finchId,
            @AuthenticationPrincipal UserDetails userDetails) {

        likeService.likeFinch(finchId, userDetails);
        return ResponseEntity.ok("Finch liked successfully.");
    }

    @DeleteMapping
    public ResponseEntity<String> unlikeFinch(
            @PathVariable UUID finchId,
            @AuthenticationPrincipal UserDetails userDetails) {

        likeService.unlikeFinch(finchId, userDetails);
        return ResponseEntity.ok("Finch unliked successfully.");
    }
}