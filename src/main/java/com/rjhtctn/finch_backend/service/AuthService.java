package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.dto.auth.LoginRequest;
import com.rjhtctn.finch_backend.dto.auth.RegisterRequest;
import com.rjhtctn.finch_backend.dto.auth.LoginResponse;
import com.rjhtctn.finch_backend.dto.user.UserResponse;
import com.rjhtctn.finch_backend.exception.ConflictException;
import com.rjhtctn.finch_backend.exception.ResourceNotFoundException;
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
            throw new ConflictException("Failed to send verification email. Please try registering again.");
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
            throw new ConflictException("Account is not verified. A new verification email has been sent.");

        } catch (BadCredentialsException e) {
            throw new ConflictException("Invalid username or password");
        }
    }

    public void verifyAccount(String token) {
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for token with username: " + username));

        if (user.getLatestVerificationJwt() == null  || user.getLatestVerificationJwt().isEmpty() ||
                !user.getLatestVerificationJwt().equals(token)) {
            throw new ConflictException("This verification link is invalid or has been superseded.");
        }

        if (user.isEnabled()) {
            System.out.println("This account is already enabled.");
            return;
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

        mailService.sendVerificationEmail(user, token);
    }
}