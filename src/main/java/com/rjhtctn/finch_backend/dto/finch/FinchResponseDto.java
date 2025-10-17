package com.rjhtctn.finch_backend.dto.finch;

import com.rjhtctn.finch_backend.dto.user.UserResponseDto;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class FinchResponseDto {
    private UUID id;
    private String content;
    private LocalDateTime createdAt;
    private UserResponseDto author;
    private List<UserResponseDto> likedUsers;
    private long likeCount;
    private long replyCount;
    private List<FinchResponseDto> replies;
    private UUID parentId;
}