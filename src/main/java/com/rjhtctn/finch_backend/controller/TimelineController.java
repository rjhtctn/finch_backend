package com.rjhtctn.finch_backend.controller;

import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.service.FinchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timeline")
public class TimelineController {

    private final FinchService finchService;

    public TimelineController(FinchService finchService) {
        this.finchService = finchService;
    }

    @GetMapping
    public ResponseEntity<Page<FinchResponseDto>> getTimeline(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {

        Page<FinchResponseDto> timeline = finchService.getTimeline(userDetails, pageable);
        return ResponseEntity.ok(timeline);
    }
}