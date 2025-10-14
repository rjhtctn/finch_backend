package com.rjhtctn.finch_backend.controller;

import com.rjhtctn.finch_backend.dto.finch.CreateFinchRequestDto;
import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.dto.finch.UpdateFinchRequestDto;
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
    public ResponseEntity<FinchResponseDto> createFinch(
            @RequestBody CreateFinchRequestDto createFinchRequestDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        FinchResponseDto createdFinch = finchService.createFinch(createFinchRequestDto, userDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdFinch);
    }

    @GetMapping
    public ResponseEntity<List<FinchResponseDto>> getAllFinches() {
        List<FinchResponseDto> finches = finchService.getAllFinches();
        return ResponseEntity.ok(finches);
    }

    @GetMapping("/{finchId}")
    public ResponseEntity<FinchResponseDto> getFinchById(@PathVariable UUID finchId) {
        FinchResponseDto finch = finchService.getFinchById(finchId);
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
}