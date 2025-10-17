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
            @RequestBody CreateFinchRequestDto createFinchRequestDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        FinchResponseDto createdFinch = finchService.createFinch(createFinchRequestDto, userDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdFinch);
    }

    @GetMapping
    public ResponseEntity<List<FinchResponseDto>> getAllFinches(@AuthenticationPrincipal  UserDetails userDetails) {
        List<FinchResponseDto> finches = finchService.getAllFinches(userDetails);
        return ResponseEntity.ok(finches);
    }

    @GetMapping("/{finchId}")
    public ResponseEntity<FinchResponseDto> getFinchById(@PathVariable UUID finchId,
                                                         @RequestParam(defaultValue = "2") int depth,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        FinchResponseDto finch = finchService.getFinchById(finchId, userDetails, depth);
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
    public ResponseEntity<FinchResponseDto> updateFinch(
            @PathVariable UUID finchId,
            @RequestBody UpdateFinchRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {

        FinchResponseDto updatedFinch = finchService.updateFinch(finchId, request, userDetails);
        return ResponseEntity.ok(updatedFinch);
    }

    @PostMapping("/{parentId}/reply")
    public ResponseEntity<FinchResponseDto> replyToFinch(
            @PathVariable UUID parentId,
            @RequestBody @Valid CreateFinchRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        FinchResponseDto response = finchService.replyToFinch(parentId, dto, userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{finchId}/likes")
    public ResponseEntity<List<UserResponseDto>> getLikedUsersOfFinch(@PathVariable UUID finchId) {
        List<UserResponseDto> likedUsers = finchService.getLikedUsersOfFinch(finchId);
        return ResponseEntity.ok(likedUsers);
    }
}