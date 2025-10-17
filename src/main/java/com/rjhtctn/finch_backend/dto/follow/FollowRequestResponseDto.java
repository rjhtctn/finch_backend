package com.rjhtctn.finch_backend.dto.follow;

import com.rjhtctn.finch_backend.dto.user.UserResponseDto;
import com.rjhtctn.finch_backend.model.FollowRequest;
import lombok.Data;
import java.time.Instant;

@Data
public class FollowRequestResponseDto {
    private Long id;
    private UserResponseDto sender;
    private UserResponseDto receiver;
    private FollowRequest.Status status;
    private Instant createdAt;
}