package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.dto.request.LoginRequest;
import com.rjhtctn.finch_backend.dto.request.RegisterRequest;
import com.rjhtctn.finch_backend.dto.response.LoginResponse;
import com.rjhtctn.finch_backend.dto.response.UserResponse;
import com.rjhtctn.finch_backend.mapper.UserMapper;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.UserRepository;
import com.rjhtctn.finch_backend.security.JwtService;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       MailService mailService,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public UserResponse registerUser(RegisterRequest request) {
        Optional<User> existingUser = userRepository
                .findByUsernameOrEmail(request.getUsername(), request.getEmail());

        if (existingUser.isPresent()) {
            throw new IllegalStateException("Username or email already taken");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(newUser);
        String token = jwtService.generateVerificationToken(savedUser);

        try {
            mailService.sendVerificationEmail(savedUser, token);
        } catch (MailException e) {
            throw new RuntimeException("Failed to send verification email. Please try registering again.", e);
        }
        return UserMapper.toUserResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLoginIdentifier(), request.getPassword())
            );

            var userDetails = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            return new LoginResponse(token);

        } catch (DisabledException e) {
            resendVerificationToken(request.getLoginIdentifier());
            throw new RuntimeException("Account is not verified. A new verification email has been sent.");

        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid username or password");
        }
    }

    public void verifyAccount(String token) {
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found for token with username: " + username));

        Date issuedAt = jwtService.extractIssuedAt(token);
        if (issuedAt.toInstant().isBefore(user.getTokenValidAfter().atZone(ZoneId.systemDefault()).toInstant())) {
            throw new RuntimeException("This verification token has been invalidated.");
        }

        if (user.isEnabled()) {
            System.out.println("This account is already enabled.");
            return;
        }
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void resendVerificationToken(String identifier) {
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + identifier));

        if (user.isEnabled()) {
            throw new IllegalStateException("This account has already been verified.");
        }

        user.setTokenValidAfter(LocalDateTime.now());
        userRepository.save(user);

        String newTokenString = jwtService.generateVerificationToken(user);
        mailService.sendVerificationEmail(user, newTokenString);
    }
}