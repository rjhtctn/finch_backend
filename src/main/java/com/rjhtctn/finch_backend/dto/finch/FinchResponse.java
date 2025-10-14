package com.rjhtctn.finch_backend.dto.finch;

import com.rjhtctn.finch_backend.dto.user.UserResponse;
import com.rjhtctn.finch_backend.model.User;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class FinchResponse {
    private UUID id;
    private String content;
    private LocalDateTime createdAt;
    private UserResponse author;
    private List<UserResponse> likedUsers;
    private long likeCount;
}