package com.rjhtctn.finch_backend.dto.finch;

import com.rjhtctn.finch_backend.dto.user.UserResponse;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FinchResponse {
    private UUID id;
    private String content;
    private LocalDateTime createdAt;
    private UserResponse author;
}