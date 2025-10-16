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

        userRepository.save(newUser);

        String token = jwtService.generateVerificationToken(newUser);
        newUser.setLatestVerificationJwt(token);
        userRepository.save(newUser);

        try {
            mailService.sendVerificationEmail(newUser, token);
        } catch (MailException e) {
            userRepository.delete(newUser);
            throw new ConflictException("Failed to send verification email. Please try again.");
        }

        return UserMapper.toUserResponse(newUser);
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getLoginIdentifier(),
                            request.getPassword()
                    )
            );

            org.springframework.security.core.userdetails.User principal =
                    (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

            User user = userService.findUserByUsername(principal.getUsername());

            String token = jwtService.generateToken(principal);
            String jwtId = jwtService.extractId(token);

            if (jwtId == null || jwtId.isBlank()) {
                throw new IllegalStateException("JWT ID could not be extracted.");
            }

            validTokenService.createTokenRecord(jwtId, user,
                    jwtService.extractIssuedAt(token),
                    jwtService.extractExpiration(token));

            return new LoginResponseDto(token);

        } catch (DisabledException e) {
            resendVerificationToken(request.getLoginIdentifier());
            throw new ConflictException("Account is not verified. A new verification email has been sent.");
        } catch (BadCredentialsException e) {
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
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for verification token."));

        if (user.isEnabled()) {
            throw new ConflictException("Account already verified.");
        }

        if (!token.equals(user.getLatestVerificationJwt())) {
            throw new ConflictException("Invalid or expired verification link.");
        }

        user.setEnabled(true);
        user.setLatestVerificationJwt(null);
        userRepository.save(user);

    }

    @Transactional
    public void resendVerificationToken(String identifier) {
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given identifier."));

        if (user.isEnabled()) {
            throw new ConflictException("Account already verified.");
        }

        String token = jwtService.generateVerificationToken(user);
        user.setLatestVerificationJwt(token);
        userRepository.save(user);

        try {
            mailService.sendVerificationEmail(user, token);
        } catch (MailException e) {
            throw new ConflictException("Could not resend verification email.");
        }
    }

    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByUsernameOrEmail(email, email).ifPresent(user -> {
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String token = jwtService.generateToken(userDetails);

            validTokenService.invalidateAllTokensForUser(user);
            validTokenService.createTokenRecord(jwtService.extractId(token), user,
                    jwtService.extractIssuedAt(token),
                    jwtService.extractExpiration(token));

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