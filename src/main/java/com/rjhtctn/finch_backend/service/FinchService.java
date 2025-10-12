package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.dto.request.CreateFinchRequest;
import com.rjhtctn.finch_backend.dto.response.FinchResponse;
import com.rjhtctn.finch_backend.mapper.FinchMapper;
import com.rjhtctn.finch_backend.model.Finch;
import com.rjhtctn.finch_backend.model.User;
import com.rjhtctn.finch_backend.repository.FinchRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class FinchService {

    private final FinchRepository finchRepository;
    private final UserService userService;

    public FinchService(FinchRepository finchRepository, UserService userService) {
        this.finchRepository = finchRepository;
        this.userService = userService;
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
}