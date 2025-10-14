package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.dto.finch.CreateFinchRequest;
import com.rjhtctn.finch_backend.dto.finch.FinchResponse;
import com.rjhtctn.finch_backend.dto.finch.UpdateFinchRequest;
import com.rjhtctn.finch_backend.dto.user.UserResponse;
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

    public FinchResponse createFinch(CreateFinchRequest createFinchRequest, UserDetails userDetails) {
        String username = userDetails.getUsername();
        User author = userService.findUserByUsername(username);

        Finch newFinch = new Finch();
        newFinch.setContent(createFinchRequest.getContent());
        newFinch.setUser(author);

        Finch savedFinch = finchRepository.save(newFinch);

        return FinchMapper.toFinchResponse(savedFinch);
    }

    public void deleteFinch(UUID finchId, UserDetails userDetails) {
        Finch finchToDelete = finchRepository.findById(finchId)
                .orElseThrow(() -> new ResourceNotFoundException("Finch not found with id: " + finchId));

        String requestingUsername = userDetails.getUsername();

        String authorUsername = finchToDelete.getUser().getUsername();

        if (!requestingUsername.equals(authorUsername)) {
            throw new AccessDeniedException("You are not authorized to delete this finch.");
        }

        finchRepository.delete(finchToDelete);
    }

    public FinchResponse updateFinch(UUID finchId, UpdateFinchRequest request, UserDetails userDetails) {
        Finch finchToUpdate = finchRepository.findById(finchId)
                .orElseThrow(() -> new ResourceNotFoundException("Finch not found with id: " + finchId));

        String requestingUsername = userDetails.getUsername();
        String authorUsername = finchToUpdate.getUser().getUsername();

        if (!requestingUsername.equals(authorUsername)) {
            throw new AccessDeniedException("You are not authorized to update this finch.");
        }

        finchToUpdate.setContent(request.getContent());
        Finch updatedFinch = finchRepository.save(finchToUpdate);

        return FinchMapper.toFinchResponse(updatedFinch);
    }

    public List<FinchResponse> getAllFinches() {
        List<Finch> finches = finchRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        return finches.stream()
                .map(this::mapToFinchResponse)
                .collect(Collectors.toList());
    }

    public Finch findFinchById(UUID finchId) {
        return finchRepository.findById(finchId)
                .orElseThrow(() -> new ResourceNotFoundException("Finch not found with id: " + finchId));
    }

    public FinchResponse getFinchById(UUID finchId) {
        Finch finch = findFinchById(finchId);
        return mapToFinchResponse(finch);
    }

    public List<FinchResponse> getFinchesByUsername(String username) {
        List<Finch> finches = finchRepository.findByUser_Username(username, Sort.by(Sort.Direction.DESC, "createdAt"));

        return finches.stream()
                .map(this::mapToFinchResponse)
                .collect(Collectors.toList());
    }

    public List<FinchResponse> getLikedFinchesByUser(User user) {
        List<Finch> likedFinches = likeService.getLikedFinchesForUser(user);

        return likedFinches.stream()
                .map(this::mapToFinchResponse)
                .collect(Collectors.toList());
    }

    private FinchResponse mapToFinchResponse(Finch finch) {
        int likeCount = likeService.getLikeCountForFinch(finch);

        List<User> likedUsers = likeService.getUsersForLikedFinch(finch);

        FinchResponse response = FinchMapper.toFinchResponse(finch);

        response.setLikeCount(likeCount);

        List<UserResponse> likedUsersDto = likedUsers.stream()
                        .map(UserMapper::toUserResponse)
                .collect(Collectors.toList());
        response.setLikedUsers(likedUsersDto);

        return response;
    }
}