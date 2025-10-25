package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.exception.ConflictException;
import com.rjhtctn.finch_backend.model.Finch;
import com.rjhtctn.finch_backend.model.Like;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.LikeRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserService userService;
    private final FinchService finchService;
    private final FollowService followService;

    public LikeService(LikeRepository likeRepository,
                       UserService userService,
                       @Lazy FinchService finchService,
                       FollowService followService) {
        this.likeRepository = likeRepository;
        this.userService = userService;
        this.finchService = finchService;
        this.followService = followService;
    }

    @Transactional
    public void likeFinch(UUID finchId, UserDetails userDetails) {
        User user = userService.findUserByUsernameOrEmail(userDetails.getUsername());
        Finch finch = finchService.findFinchById(finchId);
        User owner = finch.getUser();

        if (owner.isPrivate() && !owner.getId().equals(user.getId())) {
            boolean isFollowing = followService.getFollowing(user).stream()
                    .anyMatch(f -> f.getId().equals(owner.getId()));
            if (!isFollowing) {
                throw new AccessDeniedException("You cannot like a private user's finch.");
            }
        }

        if (likeRepository.findByUserAndFinch(user, finch).isPresent()) {
            throw new ConflictException("You have already liked this finch.");
        }

        Like newLike = new Like(user, finch);
        likeRepository.save(newLike);
    }

    @Transactional
    public void unlikeFinch(UUID finchId, UserDetails userDetails) {
        User user = userService.findUserByUsernameOrEmail(userDetails.getUsername());
        Finch finch = finchService.findFinchById(finchId);

        Like likeToDelete = likeRepository.findByUserAndFinch(user, finch)
                .orElseThrow(() -> new ConflictException("You have not liked this finch."));

        likeRepository.delete(likeToDelete);
    }

    public int getLikeCountForFinch(Finch finch) {
        return likeRepository.countByFinch(finch);
    }

    public List<User> getUsersForLikedFinch(Finch finch) {
        return likeRepository.findAllByFinch(finch).stream()
                .map(Like::getUser)
                .collect(Collectors.toList());
    }

    public List<Finch> getLikedFinchesForUser(User user) {
        return likeRepository.findAllByUser(user).stream()
                .map(Like::getFinch)
                .collect(Collectors.toList());
    }

    public boolean isLikedByUser(Finch finch, User user) {
        if (finch == null || user == null) return false;
        return likeRepository.findByUserAndFinch(user, finch).isPresent();
    }
}