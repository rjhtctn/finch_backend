package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.dto.finch.CreateFinchRequestDto;
import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.dto.finch.UpdateFinchRequestDto;
import com.rjhtctn.finch_backend.dto.user.UserResponseDto;
import com.rjhtctn.finch_backend.exception.ConflictException;
import com.rjhtctn.finch_backend.exception.ResourceNotFoundException;
import com.rjhtctn.finch_backend.mapper.FinchMapper;
import com.rjhtctn.finch_backend.mapper.UserMapper;
import com.rjhtctn.finch_backend.model.Finch;
import com.rjhtctn.finch_backend.model.Follow;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.FinchRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FinchService {

    private final FinchRepository finchRepository;
    private final UserService userService;
    private final LikeService likeService;
    private final FollowService followService;

    public FinchService(FinchRepository finchRepository,
                        UserService userService,
                        @Lazy LikeService likeService,
                        FollowService followService) {
        this.finchRepository = finchRepository;
        this.userService = userService;
        this.likeService = likeService;
        this.followService = followService;
    }

    @Transactional
    public FinchResponseDto createFinch(CreateFinchRequestDto dto, UserDetails userDetails) {
        User author = userService.findUserByUsername(userDetails.getUsername());
        Finch finch = new Finch();
        finch.setContent(dto.getContent());
        finch.setUser(author);
        Finch saved = finchRepository.save(finch);

        return enrichFinch(FinchMapper.toFinchResponse(saved));
    }

    @Transactional
    public FinchResponseDto updateFinch(UUID finchId, UpdateFinchRequestDto dto, UserDetails userDetails) {
        Finch finch = findOwnedFinch(finchId, userDetails);
        finch.setContent(dto.getContent());
        Finch updated = finchRepository.save(finch);

        return enrichFinch(FinchMapper.toFinchResponse(updated));
    }

    @Transactional
    public void deleteFinch(UUID finchId, UserDetails userDetails) {
        Finch finch = findOwnedFinch(finchId, userDetails);
        finchRepository.delete(finch);
    }

    @Transactional
    protected Finch findOwnedFinch(UUID finchId, UserDetails userDetails) {
        Finch finch = findFinchById(finchId);
        if (!finch.getUser().getUsername().equals(userDetails.getUsername()))
            throw new AccessDeniedException("You are not authorized to modify this Finch.");
        return finch;
    }

    @Transactional(readOnly = true)
    public Finch findFinchById(UUID finchId) {
        return finchRepository.findById(finchId)
                .orElseThrow(() -> new ResourceNotFoundException("Finch not found with id: " + finchId));
    }

    @Transactional(readOnly = true)
    public FinchResponseDto getFinchById(UUID finchId, UserDetails userDetails) {
        Finch finch = findFinchById(finchId);
        User currentUser = userService.findUserByUsername(userDetails.getUsername());

        if (finch.getUser().isPrivate() &&
                !finch.getUser().getId().equals(currentUser.getId()) &&
                !followService.isFollowing(currentUser, finch.getUser())) {
            throw new ConflictException("This user's account is private.");
        }

        FinchResponseDto dto = enrichFinch(FinchMapper.toFinchResponse(finch));

        dto.setReplies(finch.getReplies().stream()
                .sorted(Comparator.comparing(Finch::getCreatedAt))
                .map(r -> {
                    FinchResponseDto reply = FinchMapper.toFinchResponseWithoutReplies(r);
                    reply.setLikeCount(likeService.getLikeCountForFinch(r));
                    return reply;
                })
                .collect(Collectors.toList())
        );

        return dto;
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getAllFinches(UserDetails userDetails) {
        User currentUser = userService.findUserByUsername(userDetails.getUsername());

        return finchRepository.findByParentFinchIsNull(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(f -> {
                    User author = f.getUser();
                    boolean isSelf = author.getId().equals(currentUser.getId());
                    boolean isFollower = followService.isFollowing(currentUser, author);
                    return !author.isPrivate() || isSelf || isFollower;
                })
                .map(f -> {
                    FinchResponseDto dto = FinchMapper.toFinchResponseWithoutReplies(f);
                    dto.setLikeCount(likeService.getLikeCountForFinch(f));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getFinchesByUsername(String username, UserDetails userDetails) {
        User targetUser = userService.findUserByUsername(username);
        User currentUser = userService.findUserByUsername(userDetails.getUsername());
        boolean isSelf = targetUser.getId().equals(currentUser.getId());
        boolean isFollower = followService.isFollowing(currentUser, targetUser);

        return finchRepository.findByUser_Username(username, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(f -> !f.getUser().isPrivate() || isSelf || isFollower)
                .map(f -> {
                    FinchResponseDto dto = FinchMapper.toFinchResponseWithoutReplies(f);
                    dto.setLikeCount(likeService.getLikeCountForFinch(f));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getLikedFinchesByUser(User user) {
        return likeService.getLikedFinchesForUser(user)
                .stream()
                .map(f -> enrichFinch(FinchMapper.toFinchResponseWithoutReplies(f)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<FinchResponseDto> getTimeline(UserDetails userDetails, Pageable pageable) {
        User currentUser = userService.findUserByUsername(userDetails.getUsername());
        List<User> followingUsers = currentUser.getFollowing().stream()
                .map(Follow::getFollowing)
                .collect(Collectors.toList());
        followingUsers.add(currentUser);

        Page<Finch> page = finchRepository.findByUserIn(followingUsers, pageable);
        return page.map(f -> enrichFinch(FinchMapper.toFinchResponseWithoutReplies(f)));
    }

    @Transactional
    public FinchResponseDto replyToFinch(UUID parentId, CreateFinchRequestDto dto, UserDetails userDetails) {
        Finch parent = findFinchById(parentId);
        User author = userService.findUserByUsername(userDetails.getUsername());
        User parentOwner = parent.getUser();

        boolean isSelf = parentOwner.getId().equals(author.getId());
        boolean isFollower = followService.isFollowing(author, parentOwner);

        if (parentOwner.isPrivate() && !isSelf && !isFollower)
            throw new AccessDeniedException("You cannot reply to a private user's Finch.");

        Finch reply = new Finch();
        reply.setContent(dto.getContent());
        reply.setUser(author);
        reply.setParentFinch(parent);

        Finch saved = finchRepository.save(reply);
        return enrichFinch(FinchMapper.toFinchResponse(saved));
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getLikedUsersOfFinch(UUID finchId) {
        Finch finch = findFinchById(finchId);
        return likeService.getUsersForLikedFinch(finch)
                .stream()
                .map(UserMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    protected FinchResponseDto enrichFinch(FinchResponseDto dto) {
        Finch finch = findFinchById(dto.getId());
        dto.setLikeCount(likeService.getLikeCountForFinch(finch));
        dto.setReplyCount(finch.getReplies() != null ? finch.getReplies().size() : 0);
        return dto;
    }
}