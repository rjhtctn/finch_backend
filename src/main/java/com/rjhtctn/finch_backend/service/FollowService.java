package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.dto.user.UserResponseDto;
import com.rjhtctn.finch_backend.exception.ConflictException;
import com.rjhtctn.finch_backend.mapper.UserMapper;
import com.rjhtctn.finch_backend.model.Follow;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.FollowRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowService {

    private final UserService userService;
    private final FollowRepository followRepository;
    private final FollowRequestService followRequestService;

    public FollowService(UserService userService,
                         FollowRepository followRepository,
                         @Lazy FollowRequestService followRequestService) {
        this.userService = userService;
        this.followRepository = followRepository;
        this.followRequestService = followRequestService;
    }

    @Transactional
    public String followUser(String targetUsername, UserDetails userDetails) {
        User follower = userService.findUserByUsername(userDetails.getUsername());
        User following = userService.findUserByUsername(targetUsername);

        if (follower.equals(following)) {
            throw new ConflictException("You cannot follow yourself.");
        }

        if (isFollowing(follower, following)) {
            throw new ConflictException("You already follow this user.");
        }

        if (following.isPrivate()) {
            followRequestService.sendRequest(follower, following);
            return "requested";
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        followRepository.save(follow);
        return "followed";
    }

    @Transactional
    public void createFollow(User follower, User following) {
        if (isFollowing(follower, following)) return;
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        followRepository.save(follow);
    }

    public void unfollowUser(String usernameToUnfollow, UserDetails currentUserDetails) {
        User follower = findUserByUsername(currentUserDetails.getUsername());
        User following = findUserByUsername(usernameToUnfollow);

        Follow followToDelete = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new ConflictException("You are not following this user."));

        followRepository.delete(followToDelete);
    }

    @Transactional
    public void removeFollower(UserDetails currentUserDetails, String followerUsername) {
        User currentUser = userService.findUserByUsername(currentUserDetails.getUsername());
        User follower = userService.findUserByUsername(followerUsername);

        Follow followRecord = followRepository.findByFollowerAndFollowing(follower, currentUser)
                .orElseThrow(() -> new ConflictException(followerUsername + " is not following you."));

        followRepository.delete(followRecord);
    }

    private User findUserByUsername(String username) {
        return userService.findUserByUsername(username);
    }

    public List<UserResponseDto> getFollowers(User user) {
        List<Follow> followRecords = followRepository.findAllByFollowing(user);

        return followRecords.stream()
                .map(follow -> UserMapper.toUserResponse(follow.getFollower()))
                .collect(Collectors.toList());
    }

    public List<UserResponseDto> getFollowing(User user) {
        List<Follow> followRecords = followRepository.findAllByFollower(user);

        return followRecords.stream()
                .map(follow -> UserMapper.toUserResponse(follow.getFollowing()))
                .collect(Collectors.toList());
    }

    public boolean isFollowing(User follower, User target) {
        return followRepository.findByFollowerAndFollowing(follower, target).isPresent();
    }
}