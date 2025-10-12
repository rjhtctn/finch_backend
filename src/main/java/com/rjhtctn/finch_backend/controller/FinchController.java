package com.rjhtctn.finch_backend.controller;

import com.rjhtctn.finch_backend.dto.request.CreateFinchRequest;
import com.rjhtctn.finch_backend.dto.response.FinchResponse;
import com.rjhtctn.finch_backend.service.FinchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finch")
public class FinchController {

    private final FinchService finchService;

    public FinchController(FinchService finchService) {
        this.finchService = finchService;
    }

    @PostMapping
    public ResponseEntity<FinchResponse> createFinch(
            @RequestBody CreateFinchRequest createFinchRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        FinchResponse createdFinch = finchService.createFinch(createFinchRequest, userDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdFinch);
    }
}