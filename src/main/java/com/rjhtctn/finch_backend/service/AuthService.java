package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.dto.auth.*;
import com.rjhtctn.finch_backend.dto.user.UserResponseDto;
import com.rjhtctn.finch_backend.exception.ConflictException;
import com.rjhtctn.finch_backend.mapper.UserMapper;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.UserRepository;
import com.rjhtctn.finch_backend.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.Date;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ValidTokenService validTokenService;
    private final UserService userService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       MailService mailService,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       ValidTokenService validTokenService,
                       UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.validTokenService = validTokenService;
        this.userService = userService;
    }

    @Transactional
    public UserResponseDto registerUser(RegisterRequestDto request) {
        if (userRepository.findByUsernameOrEmail(request.getUsername(), request.getEmail()).isPresent()) {
            throw new ConflictException("Username or email already taken.");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.saveAndFlush(newUser);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                String token = generateAndSaveToken(newUser);

                try {
                    mailService.sendVerificationEmail(newUser, token);
                } catch (Exception e) {
                    logger.error("Failed to send verification email to user: {}", newUser.getEmail(), e);
                }
            }
        });

        return UserMapper.toUserResponse(newUser);
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getLoginIdentifier(),
                            request.getPassword()
                    )
            );

            User user = userService.findUserByUsernameOrEmail(request.getLoginIdentifier());

            String token = generateAndSaveToken(user);

            return new LoginResponseDto(token);

        } catch (DisabledException e) {
            resendVerificationToken(request.getLoginIdentifier());
            throw new ConflictException("Account is not verified. A new verification email has been sent.");

        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username/email or password.");
        }
    }

    @Transactional
    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalStateException("Authorization header missing or invalid.");
        }

        String token = authHeader.substring(7);
        String jwtId = jwtService.extractId(token);

        if (jwtId == null || jwtId.isBlank()) {
            throw new IllegalStateException("JWT ID not found in token.");
        }

        validTokenService.invalidateToken(jwtId);
    }

    @Transactional
    public void verifyAccount(String token) {
        String username = jwtService.extractUsername(token);
        User user = userService.findUserByUsernameOrEmail(username);

        if (user.isEnabled()) {
            throw new ConflictException("Account already verified.");
        }

        String jwtId = jwtService.extractId(token);

        if(!validTokenService.isTokenValidInDatabase(jwtId)) {
            throw new ConflictException("Invalid or expired verification token.");
        }
        user.setEnabled(true);
        validTokenService.invalidateAllTokensForUser(user);
        userRepository.save(user);
    }

    @Transactional
    public void resendVerificationToken(String email) {
        User user = userService.findUserByUsernameOrEmail(email);

        if (user.isEnabled())
            throw new ConflictException("Account already verified.");

        validTokenService.invalidateAllTokensForUser(user);

        String token = generateAndSaveToken(user);

        mailService.sendVerificationEmail(user, token);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByUsernameOrEmail(email, email).ifPresent(user -> {

            validTokenService.invalidateAllTokensForUser(user);

            String token = generateAndSaveToken(user);

            try {
                mailService.sendPasswordResetEmail(user, token);
            } catch (MailException e) {
                throw new ConflictException("Failed to send password reset email.");
            }
        });
    }

    @Transactional
    public void performPasswordReset(String token, String newPassword) {
        String username = jwtService.extractUsername(token);
        User user = userService.findUserByUsernameOrEmail(username);
        String jwtId = jwtService.extractId(token);

        if (!validTokenService.isTokenValidInDatabase(jwtId)) {
            throw new ConflictException("This password reset link is invalid or expired.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        validTokenService.invalidateAllTokensForUser(user);
    }

    private String generateAndSaveToken(User user) {
        String token = jwtService.generateToken(user);
        String jwtId = jwtService.extractId(token);
        Date tokenIssuedAt = jwtService.extractIssuedAt(token);
        Date tokenExpiration = jwtService.extractExpiration(token);

        if (jwtId == null || jwtId.isBlank()) {
            throw new IllegalStateException("JWT ID could not be extracted.");
        }

        validTokenService.createTokenRecord(jwtId, user, tokenIssuedAt, tokenExpiration);
        return token;
    }
}