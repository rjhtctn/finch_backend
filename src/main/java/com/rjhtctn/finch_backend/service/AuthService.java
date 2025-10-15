package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.dto.auth.*;
import com.rjhtctn.finch_backend.dto.user.UserResponseDto;
import com.rjhtctn.finch_backend.exception.ConflictException;
import com.rjhtctn.finch_backend.exception.ResourceNotFoundException;
import com.rjhtctn.finch_backend.mapper.UserMapper;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.UserRepository;
import com.rjhtctn.finch_backend.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ValidTokenService validTokenService;
    private final UserService userService;
    private final UserDetailsService userDetailsService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       MailService mailService,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       ValidTokenService validTokenService,
                       UserService userService,
                       UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.validTokenService = validTokenService;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    public UserResponseDto registerUser(RegisterRequestDto request) {
        if (userRepository.findByUsernameOrEmail(request.getUsername(), request.getEmail()).isPresent()) {
            throw new ConflictException("Username or email already taken");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(newUser);
        String token = jwtService.generateVerificationToken(savedUser);

        savedUser.setLatestVerificationJwt(token);
        userRepository.save(savedUser);

        try {
            mailService.sendVerificationEmail(savedUser, token);
        } catch (MailException e) {
            userRepository.delete(savedUser);
            throw new ConflictException("Failed to send verification email: " + e.getMessage());
        }
        return UserMapper.toUserResponse(savedUser);
    }

    public LoginResponseDto login(LoginRequestDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLoginIdentifier(), request.getPassword())
            );

            var userDetails = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            User user = userService.findUserByUsername(userDetails.getUsername());
            String token = jwtService.generateToken(userDetails);
            String jwtId = jwtService.extractId(token);

            if (jwtId == null || jwtId.isBlank()) {
                throw new IllegalStateException("JWT id (jti) could not be extracted");
            }

            Date issuedAt = jwtService.extractIssuedAt(token);
            Date expirationAt = jwtService.extractExpiration(token);

            validTokenService.createTokenRecord(jwtId, user,issuedAt,expirationAt);
            return new LoginResponseDto(token);

        } catch (DisabledException e) {
            resendVerificationToken(request.getLoginIdentifier());
            throw new ConflictException("Account is not verified. A new verification email has been sent.");
        }
    }

    @Transactional
    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalStateException("Authorization header missing or invalid");
        }

        String token = authHeader.substring(7);
        String jwtId = jwtService.extractId(token);
        if (jwtId == null || jwtId.isBlank()) {
            throw new IllegalStateException("JWT ID not found in token");
        }

        validTokenService.invalidateToken(jwtId);
    }

    public void verifyAccount(String token) {
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found for token with username: " + username));

        if (user.getLatestVerificationJwt() == null || !user.getLatestVerificationJwt().equals(token)) {
            throw new ConflictException("This verification link is invalid or has been superseded.");
        }

        if (user.isEnabled()) {
            throw new ConflictException("This account is already activated.");
        }

        user.setEnabled(true);
        user.setLatestVerificationJwt(null);
        userRepository.save(user);
    }

    public void resendVerificationToken(String identifier) {
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + identifier));

        if (user.isEnabled()) {
            throw new ConflictException("This account has already been verified.");
        }

        String token = jwtService.generateVerificationToken(user);
        user.setLatestVerificationJwt(token);
        userRepository.save(user);
        try {
            mailService.sendVerificationEmail(user, token);
        } catch (MailException e) {
            throw new ConflictException("Could not resend verification email: " + e.getMessage());
        }
    }

    public void requestPasswordReset(String email) {
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(email, email);
        if (userOptional.isEmpty()) return;

        User user = userOptional.get();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        Date issuedAt = jwtService.extractIssuedAt(token);
        Date expirationAt = jwtService.extractExpiration(token);

        validTokenService.invalidateAllTokensForUser(user);
        validTokenService.createTokenRecord(jwtService.extractId(token), user,issuedAt,expirationAt);

        try {
            mailService.sendPasswordResetEmail(user, token);
        } catch (MailException e) {
            throw new ConflictException("Failed to send password reset email: " + e.getMessage());
        }
    }

    public void performPasswordReset(String token, String newPassword) {
        String username = jwtService.extractUsername(token);
        User user = userService.findUserByUsername(username);
        String jwtId = jwtService.extractId(token);

        if (!validTokenService.isTokenValidInDatabase(jwtId)) {
            throw new ConflictException("This password reset link is invalid or expired.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        validTokenService.invalidateAllTokensForUser(user);
    }
}