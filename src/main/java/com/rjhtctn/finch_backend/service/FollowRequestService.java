package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.dto.follow.FollowRequestResponseDto;
import com.rjhtctn.finch_backend.exception.ConflictException;
import com.rjhtctn.finch_backend.exception.ResourceNotFoundException;
import com.rjhtctn.finch_backend.mapper.FollowRequestMapper;
import com.rjhtctn.finch_backend.model.FollowRequest;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.FollowRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowRequestService {

    private final FollowRequestRepository followRequestRepository;
    private final FollowService followService;

    public FollowRequestService(FollowRequestRepository followRequestRepository,
                                FollowService followService) {
        this.followRequestRepository = followRequestRepository;
        this.followService = followService;
    }

    @Transactional
    public void sendRequest(User sender, User receiver) {
        if (sender.equals(receiver)) {
            throw new ConflictException("You cannot follow yourself.");
        }

        if (followService.isFollowing(sender, receiver)) {
            throw new ConflictException("You already follow this user.");
        }

        followRequestRepository.findBySenderAndReceiver(sender, receiver).ifPresent(req -> {
            if (req.getStatus() == FollowRequest.Status.PENDING)
                throw new ConflictException("Follow request already sent.");
        });

        FollowRequest req = new FollowRequest();
        req.setSender(sender);
        req.setReceiver(receiver);
        followRequestRepository.save(req);
    }

    @Transactional
    public void acceptRequest(Long requestId) {
        FollowRequest req = followRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Follow request not found."));

        if (followService.isFollowing(req.getSender(), req.getReceiver())) {
            req.setStatus(FollowRequest.Status.ACCEPTED);
            followRequestRepository.save(req);
            return;
        }

        req.setStatus(FollowRequest.Status.ACCEPTED);
        followRequestRepository.save(req);
        followService.createFollow(req.getSender(), req.getReceiver());
    }

    @Transactional
    public void rejectRequest(Long requestId) {
        FollowRequest req = followRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Follow request not found."));

        switch (req.getStatus()) {
            case PENDING -> {
                req.setStatus(FollowRequest.Status.REJECTED);
                followRequestRepository.save(req);
            }
            case ACCEPTED -> {
                var receiverDetails = new org.springframework.security.core.userdetails.User(
                        req.getReceiver().getUsername(), "", List.of()
                );
                followService.unfollowUser(req.getSender().getUsername(), receiverDetails);
                req.setStatus(FollowRequest.Status.REJECTED);
                followRequestRepository.save(req);
            }
            case REJECTED -> throw new ConflictException("Follow request is already rejected.");
        }
    }

    @Transactional(readOnly = true)
    public List<FollowRequestResponseDto> getPendingRequests(User receiver) {
        return followRequestRepository.findByReceiverAndStatus(receiver, FollowRequest.Status.PENDING)
                .stream()
                .map(FollowRequestMapper::toDto)
                .collect(Collectors.toList());
    }
}