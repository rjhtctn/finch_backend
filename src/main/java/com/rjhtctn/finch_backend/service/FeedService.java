package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.dto.user.UserResponseDto;
import com.rjhtctn.finch_backend.mapper.FinchMapper;
import com.rjhtctn.finch_backend.model.*;
import com.rjhtctn.finch_backend.repository.FinchRepository;
import com.rjhtctn.finch_backend.repository.RefinchRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class FeedService {

    private final FinchRepository finchRepository;
    private final RefinchRepository refinchRepository;
    private final UserService userService;
    private final LikeService likeService;
    private final FollowService followService;
    private final BookmarkService bookmarkService;

    public FeedService(
            FinchRepository finchRepository,
            RefinchRepository refinchRepository,
            UserService userService,
            LikeService likeService,
            FollowService followService,
            BookmarkService bookmarkService) {
        this.finchRepository = finchRepository;
        this.refinchRepository = refinchRepository;
        this.userService = userService;
        this.likeService = likeService;
        this.followService = followService;
        this.bookmarkService = bookmarkService;
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getGlobalFeed(UserDetails userDetails) {
        User currentUser = userService.findUserByUsernameOrEmail(userDetails.getUsername());

        Set<UUID> followingIds = followService.getFollowing(currentUser).stream()
                .map(UserResponseDto::getId)
                .collect(Collectors.toSet());

        Predicate<User> canSeeContent = author ->
                !author.isPrivate() ||
                        author.getId().equals(currentUser.getId()) ||
                        followingIds.contains(author.getId());

        List<Finch> finches = finchRepository.findAllRootFinchesNative()
                .stream()
                .filter(finch -> canSeeContent.test(finch.getUser()))
                .toList();

        List<ReFinch> refinches = refinchRepository.findAll().stream()
                .filter(refinch -> canSeeContent.test(refinch.getUser()))
                .toList();

        return buildFeedFromSources(finches, refinches, currentUser);
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getFollowingFeed(UserDetails userDetails) {
        User currentUser = userService.findUserByUsernameOrEmail(userDetails.getUsername());

        List<User> followingUsers = currentUser.getFollowing().stream()
                .map(Follow::getFollowing)
                .collect(Collectors.toList());
        followingUsers.add(currentUser);

        List<Finch> finches = finchRepository.findByUserInAndParentFinchIsNull(
                followingUsers,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        List<ReFinch> refinches = refinchRepository.findAll().stream()
                .filter(r -> followingUsers.contains(r.getUser()))
                .toList();

        return buildFeedFromSources(finches, refinches, currentUser);
    }

    private List<FinchResponseDto> buildFeedFromSources(List<Finch> finches, List<ReFinch> refinches, User currentUser) {
        List<FeedItem> all = new ArrayList<>();

        finches.forEach(f -> all.add(new FeedItem(f, f.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant())));
        refinches.forEach(r -> all.add(new FeedItem(r.getFinch(), r.getCreatedAt())));

        all.sort(Comparator.comparing(FeedItem::createdAt).reversed());

        return all.stream()
                .map(item -> enrich(FinchMapper.toFinchResponseWithoutReplies(item.finch()), item.finch(), currentUser))
                .collect(Collectors.toList());
    }

    private FinchResponseDto enrich(FinchResponseDto dto, Finch finch, User currentUser) {
        dto.setLikeCount(likeService.getLikeCountForFinch(finch));
        dto.setReplyCount(finch.getReplies() != null ? finch.getReplies().size() : 0);
        dto.setCurrentUserLiked(likeService.isLikedByUser(finch, currentUser));
        dto.setBookmarkCount(bookmarkService.getBookmarkCount(finch));
        return dto;
    }

    private record FeedItem(Finch finch, Instant createdAt) {}
}