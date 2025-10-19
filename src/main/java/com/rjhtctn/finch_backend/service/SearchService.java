package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.dto.search.CombinedSearchResponseDto;
import com.rjhtctn.finch_backend.dto.user.UserResponseDto;
import com.rjhtctn.finch_backend.mapper.FinchMapper;
import com.rjhtctn.finch_backend.mapper.UserMapper;
import com.rjhtctn.finch_backend.model.Finch;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.FinchRepository;
import com.rjhtctn.finch_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserRepository userRepository;
    private final FinchRepository finchRepository;
    private final FollowService followService;
    private final LikeService likeService;
    private final RefinchService refinchService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> searchUsers(String query, UserDetails userDetails, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) return Page.empty(pageable);

        User currentUser = userService.findUserByUsernameOrEmail(userDetails.getUsername());

        Page<User> users = userRepository
                .findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrBioContainingIgnoreCase(
                        query, query, query, query, pageable
                );

        List<UserResponseDto> filtered = users.getContent().stream()
                .filter(u -> !u.isPrivate() || followService.isFollowing(currentUser, u) || u.getId().equals(currentUser.getId()))
                .map(UserMapper::toUserResponse)
                .toList();

        return new PageImpl<>(filtered, pageable, users.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<FinchResponseDto> searchFinches(String query, UserDetails userDetails, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) return Page.empty(pageable);

        User currentUser = userService.findUserByUsernameOrEmail(userDetails.getUsername());

        Page<Finch> finches = finchRepository.findByContentContainingIgnoreCase(query, pageable);

        List<FinchResponseDto> visibleFinches = finches.getContent().stream()
                .filter(f -> {
                    User author = f.getUser();
                    return !author.isPrivate()
                            || author.getId().equals(currentUser.getId())
                            || followService.isFollowing(currentUser, author);
                })
                .map(f -> {
                    FinchResponseDto dto = FinchMapper.toFinchResponseWithoutReplies(f);
                    dto.setLikeCount(likeService.getLikeCountForFinch(f));
                    dto.setReplyCount(f.getReplies() != null ? f.getReplies().size() : 0);
                    dto.setRepostCount(refinchService.getRepostCount(f.getId()));
                    dto.setCurrentUserLiked(likeService.isLikedByUser(f, currentUser));
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(visibleFinches, pageable, finches.getTotalElements());
    }

    @Transactional(readOnly = true)
    public CombinedSearchResponseDto searchAll(String query, UserDetails userDetails, Pageable pageable) {
        Page<UserResponseDto> users = searchUsers(query, userDetails, pageable);
        Page<FinchResponseDto> finches = searchFinches(query, userDetails, pageable);
        return new CombinedSearchResponseDto(users.getContent(), finches.getContent());
    }
}