package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.dto.user.*;
import com.rjhtctn.finch_backend.exception.ResourceNotFoundException;
import com.rjhtctn.finch_backend.mapper.UserMapper;
import com.rjhtctn.finch_backend.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.rjhtctn.finch_backend.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
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

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    public UserProfileResponseDto getOneUser(String username) {
        User user = findUserByUsername(username);
        return UserMapper.toUserProfileResponse(user);
    }

    public UserMeResponseDto updateUserProfile(String username, UpdateUserProfileRequestDto request) {
        User userToUpdate = findUserByUsername(username);
        UserMapper.updateUserFromDto(userToUpdate, request);
        User updatedUser = userRepository.save(userToUpdate);
        return UserMapper.toUserMeResponse(updatedUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public void changePassword(UserDetails userDetails, ChangePasswordRequestDto request) {
        User user = findUserByUsername(userDetails.getUsername());

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Incorrect current password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
        validTokenService.invalidateAllTokensForUser(user);
    }

    public void deleteUser(UserDetails userDetails) {
        User userToDelete =  findUserByUsername(userDetails.getUsername());

        userRepository.delete(userToDelete);
    }

    public UserMeResponseDto getMyProfile(UserDetails userDetails) {
        User user =  findUserByUsername(userDetails.getUsername());

        return UserMapper.toUserMeResponse(user);
    }

    public List<FinchResponseDto> getFinchesOfUser(String username) {
        findUserByUsername(username);

        return finchService.getFinchesByUsername(username);
    }

    public List<UserResponseDto> getFollowers(String username) {
        User user = findUserByUsername(username);

        return followService.getFollowers(user);
    }

    public List<UserResponseDto> getFollowing(String username) {
        User user = findUserByUsername(username);
        return followService.getFollowing(user);
    }

    public List<UserResponseDto> getMyFollowers(UserDetails userDetails) {
        return getFollowers(userDetails.getUsername());
    }

    public List<UserResponseDto> getMyFollowing(UserDetails userDetails) {
        return getFollowing(userDetails.getUsername());
    }

    public List<FinchResponseDto> getLikedFinchesByUsername(String username) {
        User user = findUserByUsername(username);
        return finchService.getLikedFinchesByUser(user);
    }

    public List<FinchResponseDto> getMyLikedFinches(UserDetails userDetails) {
        return getLikedFinchesByUsername(userDetails.getUsername());
    }
}