package com.rjhtctn.finch_backend.mapper;

import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.model.Finch;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FinchMapper {

    public static FinchResponseDto toFinchResponse(Finch finch) {
        return toFinchResponseWithDepth(finch, 1);
    }

    private static FinchResponseDto toFinchResponseWithDepth(Finch finch, int depth) {
        if (finch == null) return null;

        FinchResponseDto dto = toFinchResponseWithoutReplies(finch);

        if (depth < 2 && finch.getReplies() != null && !finch.getReplies().isEmpty()) {
            List<FinchResponseDto> replies = finch.getReplies().stream()
                    .sorted(Comparator.comparing(Finch::getCreatedAt))
                    .map(r -> toFinchResponseWithDepth(r, depth + 1)) // recursive
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