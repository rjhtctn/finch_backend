package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.dto.user.*;
import com.rjhtctn.finch_backend.exception.ConflictException;
import com.rjhtctn.finch_backend.exception.ResourceNotFoundException;
import com.rjhtctn.finch_backend.mapper.UserMapper;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FinchService finchService;
    private final FollowService followService;
    private final PasswordEncoder passwordEncoder;
    private final ValidTokenService validTokenService;

    public UserService(UserRepository userRepository,
                       @Lazy FinchService finchService,
                       @Lazy FollowService followService,
                       PasswordEncoder passwordEncoder,
                       ValidTokenService validTokenService) {
        this.userRepository = userRepository;
        this.finchService = finchService;
        this.followService = followService;
        this.passwordEncoder = passwordEncoder;
        this.validTokenService = validTokenService;
    }

    @Transactional(readOnly = true)
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isPrivate())
                .map(UserMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getOneUser(String username) {
        User user = findUserByUsername(username);
        checkPrivateAccess(user);
        return UserMapper.toUserProfileResponse(user);
    }

    @Transactional
    public UserMeResponseDto updateUserProfile(String username, UpdateUserProfileRequestDto request) {
        User user = findUserByUsername(username);
        UserMapper.updateUserFromDto(user, request);
        userRepository.save(user);

        return UserMapper.toUserMeResponse(user);
    }

    @Transactional
    public void changePassword(UserDetails userDetails, ChangePasswordRequestDto request) {
        User user = findUserByUsername(userDetails.getUsername());

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Incorrect current password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        validTokenService.invalidateAllTokensForUser(user);

    }

    @Transactional
    public void deleteUser(UserDetails userDetails) {
        User user = findUserByUsername(userDetails.getUsername());
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public UserMeResponseDto getMyProfile(UserDetails userDetails) {
        return UserMapper.toUserMeResponse(findUserByUsername(userDetails.getUsername()));
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getFinchesOfUser(String username) {
        User user = findUserByUsername(username);
        checkPrivateAccess(user);
        return finchService.getFinchesByUsername(username);
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getMyFinches(UserDetails userDetails) {
        return finchService.getFinchesByUsername(userDetails.getUsername());
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getFollowers(String username) {
        User user = findUserByUsername(username);
        checkPrivateAccess(user);
        return followService.getFollowers(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getFollowing(String username) {
        User user = findUserByUsername(username);
        checkPrivateAccess(user);
        return followService.getFollowing(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getMyFollowers(UserDetails userDetails) {
        return getFollowers(userDetails.getUsername());
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getMyFollowing(UserDetails userDetails) {
        return getFollowing(userDetails.getUsername());
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getLikedFinchesByUsername(String username) {
        User user = findUserByUsername(username);
        checkPrivateAccess(user);
        return finchService.getLikedFinchesByUser(user);
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getMyLikedFinches(UserDetails userDetails) {
        return getLikedFinchesByUsername(userDetails.getUsername());
    }

    @Transactional
    public void setPrivateUser(UserDetails userDetails) {
        User user = findUserByUsername(userDetails.getUsername());
        if (user.isPrivate()) {
            throw new ConflictException("User is already private.");
        }
        user.setPrivate(true);
        userRepository.save(user);
    }

    @Transactional
    public void setPublicUser(UserDetails userDetails) {
        User user = findUserByUsername(userDetails.getUsername());
        if (!user.isPrivate()) {
            throw new ConflictException("User is already public.");
        }
        user.setPrivate(false);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean isPrivate(String username) {
        return findUserByUsername(username).isPrivate();
    }

    private void checkPrivateAccess(User user) {
        if (user.isPrivate()) {
            throw new ConflictException("This user's profile is private.");
        }
    }
}