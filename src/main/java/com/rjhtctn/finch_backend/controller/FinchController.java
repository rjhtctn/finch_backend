package com.rjhtctn.finch_backend.controller;

import com.rjhtctn.finch_backend.dto.finch.CreateFinchRequestDto;
import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.dto.finch.UpdateFinchRequestDto;
import com.rjhtctn.finch_backend.dto.user.UserResponseDto;
import com.rjhtctn.finch_backend.service.FinchService;
import jakarta.validation.Valid;
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
    public ResponseEntity<FinchResponseDto> createFinch(
            @RequestBody @Valid CreateFinchRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(finchService.createFinch(dto, userDetails));
    }

    @GetMapping("/{finchId}")
    public ResponseEntity<FinchResponseDto> getFinchById(
            @PathVariable UUID finchId,
            @RequestParam(defaultValue = "2") int depth,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(finchService.getFinchById(finchId, userDetails, depth));
    }

    @PutMapping("/{finchId}")
    public ResponseEntity<FinchResponseDto> updateFinch(
            @PathVariable UUID finchId,
            @RequestBody @Valid UpdateFinchRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(finchService.updateFinch(finchId, dto, userDetails));
    }

    @DeleteMapping("/{finchId}")
    public ResponseEntity<Void> deleteFinch(
            @PathVariable UUID finchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        finchService.deleteFinch(finchId, userDetails);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{parentId}/reply")
    public ResponseEntity<FinchResponseDto> replyToFinch(
            @PathVariable UUID parentId,
            @RequestBody @Valid CreateFinchRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(finchService.replyToFinch(parentId, dto, userDetails));
    }

    @PostMapping("/{finchId}/quote")
    public ResponseEntity<FinchResponseDto> quoteFinch(
            @PathVariable UUID finchId,
            @RequestBody @Valid CreateFinchRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(finchService.quoteFinch(finchId, dto, userDetails));
    }

    @GetMapping("/{finchId}/likes")
    public ResponseEntity<List<UserResponseDto>> getLikedUsersOfFinch(@PathVariable UUID finchId) {
        return ResponseEntity.ok(finchService.getLikedUsersOfFinch(finchId));
    }
}