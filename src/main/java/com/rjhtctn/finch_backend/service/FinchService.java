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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FinchService {

    private final FinchRepository finchRepository;
    private final UserService userService;
    private final LikeService likeService;

    public FinchService(FinchRepository finchRepository,
                        UserService userService,
                        @Lazy LikeService likeService) {
        this.finchRepository = finchRepository;
        this.userService = userService;
        this.likeService = likeService;
    }

    @Transactional
    public FinchResponseDto createFinch(CreateFinchRequestDto dto, UserDetails userDetails) {
        User author = userService.findUserByUsername(userDetails.getUsername());

        Finch finch = new Finch();
        finch.setContent(dto.getContent());
        finch.setUser(author);

        Finch saved = finchRepository.save(finch);
        return FinchMapper.toFinchResponse(saved);
    }

    @Transactional
    public FinchResponseDto updateFinch(UUID finchId, UpdateFinchRequestDto dto, UserDetails userDetails) {
        Finch finch = finchRepository.findById(finchId)
                .orElseThrow(() -> new ResourceNotFoundException("Finch not found with id: " + finchId));

        String username = userDetails.getUsername();
        if (!finch.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to update this Finch.");
        }

        finch.setContent(dto.getContent());
        Finch updated = finchRepository.save(finch);
        return FinchMapper.toFinchResponse(updated);
    }

    @Transactional
    public void deleteFinch(UUID finchId, UserDetails userDetails) {
        Finch finch = finchRepository.findById(finchId)
                .orElseThrow(() -> new ResourceNotFoundException("Finch not found with id: " + finchId));

        String username = userDetails.getUsername();
        if (!finch.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to delete this Finch.");
        }

        finchRepository.delete(finch);
    }

    @Transactional(readOnly = true)
    public Finch findFinchById(UUID finchId) {
        return finchRepository.findById(finchId)
                .orElseThrow(() -> new ResourceNotFoundException("Finch not found with id: " + finchId));
    }

    @Transactional(readOnly = true)
    public FinchResponseDto getFinchById(UUID finchId) {
        Finch finch = findFinchById(finchId);
        if (finch.getUser().isPrivate()) {
            throw new ConflictException("This user's account is private.");
        }
        return mapToFinchResponse(finch);
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getAllFinches() {
        return finchRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(f -> !f.getUser().isPrivate())
                .map(this::mapToFinchResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getFinchesByUsername(String username) {
        return finchRepository.findByUser_Username(username, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(f -> !f.getUser().isPrivate())
                .map(this::mapToFinchResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FinchResponseDto> getLikedFinchesByUser(User user) {
        return likeService.getLikedFinchesForUser(user)
                .stream()
                .map(this::mapToFinchResponse)
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
        return page.map(this::mapToFinchResponse);
    }

    private FinchResponseDto mapToFinchResponse(Finch finch) {
        int likeCount = likeService.getLikeCountForFinch(finch);
        List<UserResponseDto> likedUsers = likeService.getUsersForLikedFinch(finch)
                .stream()
                .map(UserMapper::toUserResponse)
                .collect(Collectors.toList());

        FinchResponseDto dto = FinchMapper.toFinchResponse(finch);
        dto.setLikeCount(likeCount);
        dto.setLikedUsers(likedUsers);
        return dto;
    }
}