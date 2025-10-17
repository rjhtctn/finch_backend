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
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.FinchRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FinchService {

    private final FinchRepository finchRepository;
    private final UserService userService;
    private final LikeService likeService;
    private final FollowService followService;
    private final RefinchService refinchService;

    public FinchService(FinchRepository finchRepository,
                        UserService userService,
                        @Lazy LikeService likeService,
                        FollowService followService,
                        @Lazy RefinchService refinchService) {
        this.finchRepository = finchRepository;
        this.userService = userService;
        this.likeService = likeService;
        this.followService = followService;
        this.refinchService = refinchService;
    }

    @Transactional
    public FinchResponseDto createFinch(CreateFinchRequestDto dto, UserDetails userDetails) {
        User author = userService.findUserByUsername(userDetails.getUsername());
        Finch finch = new Finch();
        finch.setContent(dto.getContent());
        finch.setUser(author);
        Finch saved = finchRepository.save(finch);
        return enrichCounters(FinchMapper.toFinchResponseWithoutReplies(saved), author);
    }

    @Transactional
    public FinchResponseDto updateFinch(UUID finchId, UpdateFinchRequestDto dto, UserDetails userDetails) {
        Finch finch = findOwnedFinch(finchId, userDetails);
        finch.setContent(dto.getContent());
        Finch updated = finchRepository.save(finch);
        return enrichCounters(FinchMapper.toFinchResponseWithoutReplies(updated),
                userService.findUserByUsername(userDetails.getUsername()));
    }

    @Transactional
    public void deleteFinch(UUID finchId, UserDetails userDetails) {
        Finch finch = findOwnedFinch(finchId, userDetails);
        finchRepository.delete(finch);
    }

    @Transactional
    protected Finch findOwnedFinch(UUID finchId, UserDetails userDetails) {
        Finch finch = findFinchById(finchId);
        if (!finch.getUser().getUsername().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("You are not authorized to modify this Finch.");
        }
        return finch;
    }

    @Transactional(readOnly = true)
    public Finch findFinchById(UUID finchId) {
        return finchRepository.findById(finchId)
                .orElseThrow(() -> new ResourceNotFoundException("Finch not found with id: " + finchId));
    }

    @Transactional(readOnly = true)
    public FinchResponseDto getFinchById(UUID finchId, UserDetails userDetails, int depth) {
        Finch finch = findFinchById(finchId);
        User currentUser = userService.findUserByUsername(userDetails.getUsername());

        if (finch.getUser().isPrivate()
                && !finch.getUser().getId().equals(currentUser.getId())
                && !followService.isFollowing(currentUser, finch.getUser())) {
            throw new ConflictException("This user's account is private.");
        }

        FinchResponseDto dto = FinchMapper.toFinchResponse(finch, depth);

        dto.setLikeCount(likeService.getLikeCountForFinch(finch));
        dto.setReplyCount(finch.getReplies() != null ? finch.getReplies().size() : 0);
        dto.setCurrentUserLiked(likeService.isLikedByUser(finch, currentUser));

        if (dto.getReplies() != null && depth > 0) {
            dto.getReplies().forEach(r -> {
                Finch replyEntity = findFinchById(r.getId());
                r.setLikeCount(likeService.getLikeCountForFinch(replyEntity));
                r.setReplyCount(replyEntity.getReplies() != null ? replyEntity.getReplies().size() : 0);
                r.setCurrentUserLiked(likeService.isLikedByUser(replyEntity, currentUser));
            });
        }

        return dto;
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
                    dto.setReplyCount(f.getReplies() != null ? f.getReplies().size() : 0);
                    dto.setCurrentUserLiked(likeService.isLikedByUser(f, currentUser));
                    dto.setRepostCount(refinchService.getRepostCount(f.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getLikedFinchesByUser(User user) {
        return likeService.getLikedFinchesForUser(user)
                .stream()
                .map(f -> enrichCounters(FinchMapper.toFinchResponseWithoutReplies(f), user))
                .collect(Collectors.toList());
    }

    @Transactional
    public FinchResponseDto replyToFinch(UUID parentId, CreateFinchRequestDto dto, UserDetails userDetails) {
        Finch parent = findFinchById(parentId);
        User author = userService.findUserByUsername(userDetails.getUsername());
        User parentOwner = parent.getUser();

        boolean isSelf = parentOwner.getId().equals(author.getId());
        boolean isFollower = followService.isFollowing(author, parentOwner);

        if (parentOwner.isPrivate() && !isSelf && !isFollower) {
            throw new AccessDeniedException("You cannot reply to a private user's Finch.");
        }

        if (refinchService.isRepost(parent)) {
            throw new ConflictException("You cannot reply to a repost.");
        }

        Finch reply = new Finch();
        reply.setContent(dto.getContent());
        reply.setUser(author);
        reply.setParentFinch(parent);

        Finch saved = finchRepository.save(reply);
        return enrichCounters(FinchMapper.toFinchResponseWithoutReplies(saved), author);
    }

    @Transactional
    public FinchResponseDto quoteFinch(UUID quotedId, CreateFinchRequestDto dto, UserDetails userDetails) {
        Finch quoted = findFinchById(quotedId);
        User author = userService.findUserByUsername(userDetails.getUsername());

        if (quoted.getUser().isPrivate()
                && !quoted.getUser().getId().equals(author.getId())
                && !followService.isFollowing(author, quoted.getUser())) {
            throw new ConflictException("This user's account is private.");
        }

        Finch quote = new Finch();
        quote.setUser(author);
        quote.setContent(dto.getContent());
        quote.setQuotedFinch(quoted);

        Finch saved = finchRepository.save(quote);
        return enrichCounters(FinchMapper.toFinchResponseWithoutReplies(saved), author);
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
    protected FinchResponseDto enrichCounters(FinchResponseDto dto, User currentUser) {
        Finch finch = findFinchById(dto.getId());
        dto.setLikeCount(likeService.getLikeCountForFinch(finch));
        dto.setReplyCount(finch.getReplies() != null ? finch.getReplies().size() : 0);
        dto.setCurrentUserLiked(likeService.isLikedByUser(finch, currentUser));
        dto.setRepostCount(refinchService.getRepostCount(finch.getId()));
        return dto;
    }
}