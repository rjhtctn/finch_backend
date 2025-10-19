package com.rjhtctn.finch_backend.controller;

import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.dto.search.CombinedSearchResponseDto;
import com.rjhtctn.finch_backend.dto.user.UserResponseDto;
import com.rjhtctn.finch_backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDto>> searchUsers(
            @RequestParam("q") String query,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "username", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(searchService.searchUsers(query, userDetails, pageable));
    }

    @GetMapping("/finches")
    public ResponseEntity<Page<FinchResponseDto>> searchFinches(
            @RequestParam("q") String query,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(searchService.searchFinches(query, userDetails, pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<CombinedSearchResponseDto> searchAll(
            @RequestParam("q") String query,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(searchService.searchAll(query, userDetails, pageable));
    }
}