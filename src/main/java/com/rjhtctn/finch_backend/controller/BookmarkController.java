package com.rjhtctn.finch_backend.controller;

import com.rjhtctn.finch_backend.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookmark")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;
    private final com.rjhtctn.finch_backend.service.FinchService finchService;
    private final com.rjhtctn.finch_backend.service.UserService userService;

    @PostMapping("/{finchId}/toggle")
    public ResponseEntity<Void> toggleBookmark(
            @PathVariable UUID finchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        bookmarkService.toggleBookmark(finchId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<com.rjhtctn.finch_backend.dto.finch.FinchResponseDto>> getBookmarks(
            @AuthenticationPrincipal UserDetails userDetails) {
        var user = userService.findUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(finchService.getBookmarkedFinchesByUser(user));
    }
}