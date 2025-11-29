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
import com.rjhtctn.finch_backend.model.FinchImage;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.FinchRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FinchService {

    private final FinchRepository finchRepository;
    private final UserService userService;
    private final LikeService likeService;
    private final FollowService followService;
    private final RefinchService refinchService;
    private final ImageKitService imageKitService;
    private final BookmarkService bookmarkService;

    public FinchService(FinchRepository finchRepository,
                        UserService userService,
                        @Lazy LikeService likeService,
                        FollowService followService,
                        @Lazy RefinchService refinchService,
                        ImageKitService imageKitService,
                        BookmarkService bookmarkService) {
        this.finchRepository = finchRepository;
        this.userService = userService;
        this.likeService = likeService;
        this.followService = followService;
        this.refinchService = refinchService;
        this.imageKitService = imageKitService;
        this.bookmarkService = bookmarkService;
    }

    @Transactional
    public FinchResponseDto createFinch(CreateFinchRequestDto dto, List<MultipartFile> images, UserDetails userDetails) {
        User author = userService.findUserByUsernameOrEmail(userDetails.getUsername());

        Finch finch = new Finch();
        finch.setContent(dto.getContent());
        finch.setUser(author);
        finch = finchRepository.save(finch);

        if (images != null && !images.isEmpty()) {
            if (images.size() > 4)
                throw new ConflictException("En fazla 4 fotoğraf yüklenebilir.");

            for (MultipartFile image : images) {
                if (image.isEmpty()) continue;

                String folderPath = String.format("finch/%s/posts/%s", author.getUsername(), finch.getId());

                String imageUrl = imageKitService.uploadImage(image, folderPath);

                FinchImage img = new FinchImage();
                img.setImageUrl(imageUrl);
                img.setFileId(imageKitService.getLastFileId());
                img.setFinch(finch);
                finch.getImages().add(img);
            }

            finch = finchRepository.save(finch);
        }

        return enrichCounters(FinchMapper.toFinchResponseWithoutReplies(finch), author);
    }

    @Transactional
    public FinchResponseDto updateFinch(UUID finchId,
                                        UpdateFinchRequestDto dto,
                                        List<MultipartFile> newImages,
                                        UserDetails userDetails) {
        Finch finch = findOwnedFinch(finchId, userDetails);
        User author = userService.findUserByUsernameOrEmail(userDetails.getUsername());

        Set<String> existingIds = finch.getImages() == null ? Set.of() :
                finch.getImages().stream()
                        .map(FinchImage::getFileId)
                        .collect(Collectors.toSet());

        Set<String> deleteIds = dto != null && dto.getDeleteImageIds() != null
                ? dto.getDeleteImageIds().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(existingIds::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                : Set.of();

        int currentCount = existingIds.size();

        int newCount = newImages == null ? 0 :
                (int) newImages.stream().filter(f -> f != null && !f.isEmpty()).count();

        int totalAfterUpdate = currentCount - deleteIds.size() + newCount;
        if (totalAfterUpdate > 4) {
            throw new ConflictException(String.format(
                    "Toplam resim sayısı 4'ü geçemez. (Mevcut: %d, Silinecek: %d, Eklenecek: %d, Sonuç: %d)",
                    currentCount, deleteIds.size(), newCount, totalAfterUpdate
            ));
        }

        if (dto != null && dto.getContent() != null) {
            finch.setContent(dto.getContent());
        }

        if (!deleteIds.isEmpty() && finch.getImages() != null) {
            List<FinchImage> toDelete = finch.getImages().stream()
                    .filter(img -> deleteIds.contains(img.getFileId()))
                    .toList();

            toDelete.forEach(img -> imageKitService.deleteImage(img.getFileId()));
            finch.getImages().removeAll(toDelete);

            if (finch.getImages().isEmpty()) {
                String folderPath = String.format("finch/%s/posts/%s", author.getUsername(), finch.getId());
                imageKitService.deleteFolder(folderPath);
            }
        }

        if (newImages != null && !newImages.isEmpty()) {
            String folderPath = String.format("finch/%s/posts/%s", author.getUsername(), finch.getId());
            for (MultipartFile image : newImages) {
                if (image == null || image.isEmpty()) continue;
                String imageUrl = imageKitService.uploadImage(image, folderPath);

                FinchImage img = new FinchImage();
                img.setImageUrl(imageUrl);
                img.setFileId(imageKitService.getLastFileId());
                img.setFinch(finch);
                finch.getImages().add(img);
            }
        }

        Finch updated = finchRepository.save(finch);
        return enrichCounters(FinchMapper.toFinchResponseWithoutReplies(updated), author);
    }

    @Transactional
    public void deleteFinch(UUID finchId, UserDetails userDetails) {
        Finch finch = findOwnedFinch(finchId, userDetails);

        if (finch.getImages() != null && !finch.getImages().isEmpty()) {
            imageKitService.deleteFolder("finch/" + finch.getUser().getUsername() + "/posts/" +  finch.getId());
        }
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
        User currentUser = userService.findUserByUsernameOrEmail(userDetails.getUsername());

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
        User targetUser = userService.findUserByUsernameOrEmail(username);
        User currentUser = userService.findUserByUsernameOrEmail(userDetails.getUsername());
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
    public FinchResponseDto replyToFinch(UUID parentId, CreateFinchRequestDto dto, List<MultipartFile> images, UserDetails userDetails) {
        Finch parent = findFinchById(parentId);
        User author = userService.findUserByUsernameOrEmail(userDetails.getUsername());
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

        if (images != null && !images.isEmpty()) {
            if (images.size() > 4)
                throw new ConflictException("En fazla 4 fotoğraf yüklenebilir.");

            for (MultipartFile image : images) {
                if (image.isEmpty()) continue;

                String folderPath = String.format("finch/%s/posts/%s", author.getUsername(), reply.getId());

                String imageUrl = imageKitService.uploadImage(image, folderPath);

                FinchImage img = new FinchImage();
                img.setImageUrl(imageUrl);
                img.setFileId(imageKitService.getLastFileId());
                img.setFinch(reply);
                reply.getImages().add(img);
            }

            saved = finchRepository.save(reply);
        }

        return enrichCounters(FinchMapper.toFinchResponseWithoutReplies(saved), author);
    }

    @Transactional
    public FinchResponseDto quoteFinch(UUID quotedId, CreateFinchRequestDto dto, List<MultipartFile> images, UserDetails userDetails) {
        Finch quoted = findFinchById(quotedId);
        User author = userService.findUserByUsernameOrEmail(userDetails.getUsername());

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

        if (images != null && !images.isEmpty()) {
            if (images.size() > 4)
                throw new ConflictException("En fazla 4 fotoğraf yüklenebilir.");

            for (MultipartFile image : images) {
                if (image.isEmpty()) continue;

                String folderPath = String.format("finch/%s/posts/%s", author.getUsername(), quote.getId());

                String imageUrl = imageKitService.uploadImage(image, folderPath);

                FinchImage img = new FinchImage();
                img.setImageUrl(imageUrl);
                img.setFileId(imageKitService.getLastFileId());
                img.setFinch(quote);
                quote.getImages().add(img);
            }

            saved = finchRepository.save(quote);
        }

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
        dto.setBookmarkCount(bookmarkService.getBookmarkCount(finch));
        return dto;
    }
}