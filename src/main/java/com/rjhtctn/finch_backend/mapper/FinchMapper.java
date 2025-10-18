package com.rjhtctn.finch_backend.mapper;

import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.model.Finch;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FinchMapper {

    public static FinchResponseDto toFinchResponse(Finch finch, int depth) {
        if (finch == null) return null;
        FinchResponseDto dto = toFinchResponseWithoutReplies(finch);

        if (depth > 0 && finch.getReplies() != null && !finch.getReplies().isEmpty()) {
            List<FinchResponseDto> replies = finch.getReplies().stream()
                    .sorted(Comparator.comparing(Finch::getCreatedAt))
                    .map(r -> toFinchResponse(r, depth - 1))
                    .collect(Collectors.toList());
            dto.setReplies(replies);
        }
        return dto;
    }

    public static FinchResponseDto toFinchResponseWithoutReplies(Finch finch) {
        if (finch == null) return null;
        FinchResponseDto dto = new FinchResponseDto();
        dto.setId(finch.getId());
        dto.setContent(finch.getContent());
        dto.setCreatedAt(finch.getCreatedAt());
        dto.setAuthor(UserMapper.toUserResponse(finch.getUser()));
        dto.setImageUrl(finch.getImageUrl());
        if (finch.getParentFinch() != null) {
            dto.setParentId(finch.getParentFinch().getId());
        }
        return dto;
    }
}