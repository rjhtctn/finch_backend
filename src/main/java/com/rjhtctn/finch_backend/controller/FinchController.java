package com.rjhtctn.finch_backend.controller;

import com.rjhtctn.finch_backend.dto.finch.CreateFinchRequest;
import com.rjhtctn.finch_backend.dto.finch.FinchResponse;
import com.rjhtctn.finch_backend.dto.finch.UpdateFinchRequest;
import com.rjhtctn.finch_backend.service.FinchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping
    public ResponseEntity<List<FinchResponse>> getAllFinches() {
        List<FinchResponse> finches = finchService.getAllFinches();
        return ResponseEntity.ok(finches);
    }

    @GetMapping("/{finchId}")
    public ResponseEntity<FinchResponse> getFinchById(@PathVariable UUID finchId) {
        FinchResponse finch = finchService.getFinchById(finchId);
        return ResponseEntity.ok(finch);
    }

    @DeleteMapping("/{finchId}")
    public ResponseEntity<Void> deleteFinch(
            @PathVariable UUID finchId,
            @AuthenticationPrincipal UserDetails userDetails) {

        finchService.deleteFinch(finchId, userDetails);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{finchId}")
    public ResponseEntity<FinchResponse> updateFinch(
            @PathVariable UUID finchId,
            @RequestBody UpdateFinchRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        FinchResponse updatedFinch = finchService.updateFinch(finchId, request, userDetails);
        return ResponseEntity.ok(updatedFinch);
    }

    @GetMapping("/me")
    public ResponseEntity<List<FinchResponse>> getMyFinches(@AuthenticationPrincipal UserDetails userDetails) {
        List<FinchResponse> myFinches = finchService.getMyFinches(userDetails);

        return ResponseEntity.ok(myFinches);
    }
}