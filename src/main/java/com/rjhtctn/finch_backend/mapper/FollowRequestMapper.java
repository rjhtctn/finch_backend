package com.rjhtctn.finch_backend.mapper;

import com.rjhtctn.finch_backend.dto.follow.FollowRequestResponseDto;
import com.rjhtctn.finch_backend.model.FollowRequest;

public class FollowRequestMapper {

    public static FollowRequestResponseDto toDto(FollowRequest req) {
        FollowRequestResponseDto dto = new FollowRequestResponseDto();
        dto.setId(req.getId());
        dto.setSender(UserMapper.toUserResponse(req.getSender()));
        dto.setReceiver(UserMapper.toUserResponse(req.getReceiver()));
        dto.setStatus(req.getStatus());
        dto.setCreatedAt(req.getCreatedAt());
        return dto;
    }
}