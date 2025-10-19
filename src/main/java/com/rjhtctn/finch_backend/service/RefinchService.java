package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.exception.ConflictException;
import com.rjhtctn.finch_backend.exception.ResourceNotFoundException;
import com.rjhtctn.finch_backend.model.*;
import com.rjhtctn.finch_backend.repository.RefinchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetails;

@Service
public class RefinchService {

    private final RefinchRepository refinchRepository;
    private final FinchService finchService;
    private final UserService userService;

    public RefinchService(RefinchRepository refinchRepository,
                         FinchService finchService,
                         UserService userService) {
        this.refinchRepository = refinchRepository;
        this.finchService = finchService;
        this.userService = userService;
    }

    @Transactional
    public void repostFinch(java.util.UUID finchId, UserDetails userDetails) {
        User user = userService.findUserByUsernameOrEmail(userDetails.getUsername());
        Finch finch = finchService.findFinchById(finchId);

        if (finch.getUser().getId().equals(user.getId())) {
            throw new ConflictException("You cannot repost your own Finch.");
        }

        if (refinchRepository.existsByUserAndFinch(user, finch)) {
            throw new ConflictException("You have already reposted this Finch.");
        }

        ReFinch repost = new ReFinch();
        repost.setUser(user);
        repost.setFinch(finch);
        refinchRepository.save(repost);
    }

    @Transactional
    public void removeRepost(java.util.UUID finchId, UserDetails userDetails) {
        User user = userService.findUserByUsernameOrEmail(userDetails.getUsername());
        Finch finch = finchService.findFinchById(finchId);
        ReFinch repost = refinchRepository.findByUserAndFinch(user, finch)
                .orElseThrow(() -> new ResourceNotFoundException("Repost not found."));
        refinchRepository.delete(repost);
    }

    @Transactional(readOnly = true)
    public long getRepostCount(java.util.UUID finchId) {
        Finch finch = finchService.findFinchById(finchId);
        return refinchRepository.countByFinch(finch);
    }

    @Transactional(readOnly = true)
    public boolean isRepost(Finch finch) {
        return refinchRepository.existsByFinch(finch);
    }
}
