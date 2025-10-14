package com.rjhtctn.finch_backend.controller;

import com.rjhtctn.finch_backend.dto.auth.LoginRequest;
import com.rjhtctn.finch_backend.dto.auth.RegisterRequest;
import com.rjhtctn.finch_backend.dto.auth.ResendTokenRequest;
import com.rjhtctn.finch_backend.dto.auth.LoginResponse;
import com.rjhtctn.finch_backend.dto.user.UserResponse;
import com.rjhtctn.finch_backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("Logout successful. Token invalidated.");
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        authService.verifyAccount(token);
        return ResponseEntity.ok("Account Activated Successfully!");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationToken(@Valid @RequestBody ResendTokenRequest request) {
        authService.resendVerificationToken(request.getEmail());
        return ResponseEntity.ok("If an account with this email exists, a new verification link has been sent.");
    }
}