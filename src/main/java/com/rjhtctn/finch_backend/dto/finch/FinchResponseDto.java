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

    private List<ImageResponse> images;

    private long likeCount;
    private int replyCount;
    private long bookmarkCount;

    private List<FinchResponseDto> replies;

    private long repostCount;
    private FinchResponseDto quotedFinch;

    private UUID parentId;

    private boolean currentUserLiked;

    private String repostedBy;

    @Data
    public static class ImageResponse {
        private String imageUrl;
        private String fileId;
    }
}