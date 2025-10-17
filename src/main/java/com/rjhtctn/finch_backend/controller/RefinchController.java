package com.rjhtctn.finch_backend.controller;

import com.rjhtctn.finch_backend.service.RefinchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/repost")
public class RefinchController {

    private final RefinchService refinchService;

    public RefinchController(RefinchService refinchService) {
        this.refinchService = refinchService;
    }

    @PostMapping("/{finchId}")
    public ResponseEntity<String> repost(@PathVariable UUID finchId,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        refinchService.repostFinch(finchId, userDetails);
        return ResponseEntity.ok("Reposted successfully.");
    }

    @DeleteMapping("/{finchId}")
    public ResponseEntity<String> removeRepost(@PathVariable UUID finchId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        refinchService.removeRepost(finchId, userDetails);
        return ResponseEntity.ok("Repost removed.");
    }
}