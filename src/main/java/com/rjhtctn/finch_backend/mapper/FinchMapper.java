package com.rjhtctn.finch_backend.mapper;

import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.model.Finch;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FinchMapper {

    public static FinchResponseDto toFinchResponse(Finch finch, int depth) {
        return toFinchResponseRecursive(finch, 1, depth);
    }

    private static FinchResponseDto toFinchResponseRecursive(Finch finch, int currentDepth, int maxDepth) {
        if (finch == null) return null;

        FinchResponseDto dto = toFinchResponseWithoutReplies(finch);

        if (currentDepth < maxDepth && finch.getReplies() != null && !finch.getReplies().isEmpty()) {
            List<FinchResponseDto> replies = finch.getReplies().stream()
                    .sorted(Comparator.comparing(Finch::getCreatedAt))
                    .map(r -> toFinchResponseRecursive(r, currentDepth + 1, maxDepth))
                    .collect(Collectors.toList());
            dto.setReplies(replies);
        }

        return dto;
    }

    public static FinchResponseDto toFinchResponseWithoutReplies(Finch finch) {
        FinchResponseDto dto = new FinchResponseDto();
        dto.setId(finch.getId());
        dto.setContent(finch.getContent());
        dto.setCreatedAt(finch.getCreatedAt());
        dto.setAuthor(UserMapper.toUserResponse(finch.getUser()));
        if (finch.getParentFinch() != null) {
            dto.setParentId(finch.getParentFinch().getId());
        }
        return dto;
    }
}