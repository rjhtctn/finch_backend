package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.dto.user.*;
import com.rjhtctn.finch_backend.exception.ConflictException;
import com.rjhtctn.finch_backend.exception.ResourceNotFoundException;
import com.rjhtctn.finch_backend.mapper.UserMapper;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.UserRepository;
import com.rjhtctn.finch_backend.security.JwtService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FinchService finchService;
    private final FollowService followService;
    private final PasswordEncoder passwordEncoder;
    private final ValidTokenService validTokenService;
    private final ImageKitService imageKitService;
    private final MailService mailService;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,
                       @Lazy FinchService finchService,
                       @Lazy FollowService followService,
                       PasswordEncoder passwordEncoder,
                       ValidTokenService validTokenService,
                       ImageKitService imageKitService,
                       MailService mailService,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.finchService = finchService;
        this.followService = followService;
        this.passwordEncoder = passwordEncoder;
        this.validTokenService = validTokenService;
        this.imageKitService = imageKitService;
        this.mailService = mailService;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public User findUserByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username or email: " + usernameOrEmail
                ));
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getOneUser(String username) {
        User user = findUserByUsernameOrEmail(username);
        return UserMapper.toUserProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public UserMeResponseDto getMyProfile(UserDetails userDetails) {
        return UserMapper.toUserMeResponse(findUserByUsernameOrEmail(userDetails.getUsername()));
    }

    @Transactional
    public UserMeResponseDto updateUserProfile(String username, UpdateUserProfileRequestDto request) {
        User user = findUserByUsernameOrEmail(username);
        UserMapper.updateUserFromDto(user, request);
        userRepository.save(user);
        return UserMapper.toUserMeResponse(user);
    }

    @Transactional
    public UserMeResponseDto updateProfileImage(UserDetails userDetails, MultipartFile file) {
        return uploadUserImage(userDetails, file, "ProfileImages", User::setProfileImageUrl);
    }

    @Transactional
    public UserMeResponseDto updateBannerImage(UserDetails userDetails, MultipartFile file) {
        return uploadUserImage(userDetails, file, "BannerImages", User::setBannerImageUrl);
    }

    private UserMeResponseDto uploadUserImage(UserDetails userDetails, MultipartFile file, String folderName, java.util.function.BiConsumer<User, String> imageSetter) {
        User user = findUserByUsernameOrEmail(userDetails.getUsername());
        if (file.isEmpty()) {
            throw new BadCredentialsException("Invalid data provided");
        }

        String folderPath = String.format("finch/%s/%s", user.getUsername(), folderName);
        String imageUrl = imageKitService.uploadImage(file, folderPath);

        imageSetter.accept(user, imageUrl);
        userRepository.save(user);

        return UserMapper.toUserMeResponse(user);
    }

    @Transactional
    public void changePassword(UserDetails userDetails, ChangePasswordRequestDto request) {
        User user = findUserByUsernameOrEmail(userDetails.getUsername());

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Incorrect current password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        validTokenService.invalidateAllTokensForUser(user);
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        String jwtId = jwtService.extractId(token);
        Date tokenIssuedAt = jwtService.extractIssuedAt(token);
        Date tokenExpirationAt = jwtService.extractExpiration(token);
        validTokenService.createTokenRecord(jwtId,user,tokenIssuedAt,tokenExpirationAt);
        userRepository.save(user);
        mailService.sendVerificationEmail(user, token);
    }

    @Transactional
    public void changeEmail(UserDetails userDetails, ChangeEmailRequestDto request) {
        User user = findUserByUsernameOrEmail(userDetails.getUsername());
        if (Objects.equals(user.getEmail(), request.getEmail())) {
            throw new BadCredentialsException("This email address is already in use.");
        }
        user.setEmail(request.getEmail());
        validTokenService.invalidateAllTokensForUser(user);
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        String jwtId = jwtService.extractId(token);
        Date tokenIssuedAt = jwtService.extractIssuedAt(token);
        Date tokenExpirationAt = jwtService.extractExpiration(token);
        validTokenService.createTokenRecord(jwtId,user,tokenIssuedAt,tokenExpirationAt);
        userRepository.save(user);
        mailService.sendEmailChanged(user, token);
    }

    @Transactional
    public void deleteUser(UserDetails userDetails) {
        User user = findUserByUsernameOrEmail(userDetails.getUsername());
        imageKitService.deleteFolder("/finch/" + user.getUsername());
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getFinchesOfUser(String username, UserDetails requester) {
        User user = findUserByUsernameOrEmail(username);
        checkPrivateAccess(user, requester);
        return finchService.getFinchesByUsername(username, requester);
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getMyFinches(UserDetails userDetails) {
        return finchService.getFinchesByUsername(userDetails.getUsername(), userDetails);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getFollowers(String username, UserDetails requester) {
        User user = findUserByUsernameOrEmail(username);
        checkPrivateAccess(user, requester);
        return followService.getFollowers(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getFollowing(String username, UserDetails requester) {
        User user = findUserByUsernameOrEmail(username);
        checkPrivateAccess(user, requester);
        return followService.getFollowing(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getMyFollowers(UserDetails userDetails) {
        return getFollowers(userDetails.getUsername(), userDetails);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getMyFollowing(UserDetails userDetails) {
        return getFollowing(userDetails.getUsername(), userDetails);
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getLikedFinchesByUsername(String username, UserDetails requester) {
        User user = findUserByUsernameOrEmail(username);
        checkPrivateAccess(user, requester);
        return finchService.getLikedFinchesByUser(user);
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getMyLikedFinches(UserDetails userDetails) {
        return getLikedFinchesByUsername(userDetails.getUsername(), userDetails);
    }

    @Transactional
    public void setPrivateUser(UserDetails userDetails) {
        User user = findUserByUsernameOrEmail(userDetails.getUsername());
        if (user.isPrivate()) throw new ConflictException("User is already private.");
        user.setPrivate(true);
        userRepository.save(user);
    }

    @Transactional
    public void setPublicUser(UserDetails userDetails) {
        User user = findUserByUsernameOrEmail(userDetails.getUsername());
        if (!user.isPrivate()) throw new ConflictException("User is already public.");
        user.setPrivate(false);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    protected void checkPrivateAccess(User targetUser, UserDetails requesterDetails) {
        if (!targetUser.isPrivate()) return;

        if (requesterDetails == null)
            throw new ConflictException("This user's profile is private.");

        User requester = findUserByUsernameOrEmail(requesterDetails.getUsername());

        if (targetUser.getId().equals(requester.getId()))
            return;

        boolean isFollower = followService.isFollowing(requester, targetUser);
        if (isFollower)
            return;

        throw new ConflictException("This user's profile is private.");
    }
}