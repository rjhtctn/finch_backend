package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.exception.ConflictException;
import com.rjhtctn.finch_backend.model.Follow;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.FollowRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class FollowService {

    private final UserService userService;
    private final FollowRepository followRepository;

    public FollowService(UserService userService, FollowRepository followRepository) {
        this.userService = userService;
        this.followRepository = followRepository;
    }

    public void followUser(String usernameToFollow, UserDetails currentUserDetails) {
        User follower = findUserByUsername(currentUserDetails.getUsername());
        User following = findUserByUsername(usernameToFollow);

        if (follower.getId().equals(following.getId())) {
            throw new ConflictException("You cannot follow yourself.");
        }

        if (followRepository.findByFollowerAndFollowing(follower, following).isPresent()) {
            throw new ConflictException("You are already following this user.");
        }

        Follow newFollow = new Follow(follower, following);
        followRepository.save(newFollow);
    }

    public void unfollowUser(String usernameToUnfollow, UserDetails currentUserDetails) {
        User follower = findUserByUsername(currentUserDetails.getUsername());
        User following = findUserByUsername(usernameToUnfollow);

        Follow followToDelete = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new ConflictException("You are not following this user."));

        followRepository.delete(followToDelete);
    }

    private User findUserByUsername(String username) {
        return userService.findUserByUsername(username);
    }
}