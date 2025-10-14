package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.exception.ConflictException;
import com.rjhtctn.finch_backend.model.Finch;
import com.rjhtctn.finch_backend.model.Like;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.LikeRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserService userService;
    private final FinchService finchService;

    public LikeService(LikeRepository likeRepository, UserService userService, FinchService finchService) {
        this.likeRepository = likeRepository;
        this.userService = userService;
        this.finchService = finchService;
    }

    public void likeFinch(UUID finchId, UserDetails userDetails) {
        User user = userService.findUserByUsername(userDetails.getUsername());
        Finch finch = finchService.findFinchById(finchId);

        if (likeRepository.findByUserAndFinch(user, finch).isPresent()) {
            throw new ConflictException("You have already liked this finch.");
        }

        Like newLike = new Like(user, finch);
        likeRepository.save(newLike);
    }

    public void unlikeFinch(UUID finchId, UserDetails userDetails) {
        User user = userService.findUserByUsername(userDetails.getUsername());
        Finch finch = finchService.findFinchById(finchId);

        Like likeToDelete = likeRepository.findByUserAndFinch(user, finch)
                .orElseThrow(() -> new ConflictException("You have not liked this finch."));

        likeRepository.delete(likeToDelete);
    }

    public int getLikeCountForFinch(Finch finch) {
        return likeRepository.countByFinch(finch);
    }

    public List<User> getUsersForLikedFinch(Finch finch) {
        List<Like> likes = likeRepository.findAllByFinch(finch);

        return likes.stream()
                .map(Like::getUser)
                .collect(Collectors.toList());
    }

    public List<Finch> getLikedFinchesForUser(User user) {
        List<Like> likes = likeRepository.findAllByUser(user);

        return likes.stream()
                .map(Like::getFinch)
                .collect(Collectors.toList());
    }
}