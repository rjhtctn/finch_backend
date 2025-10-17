package com.rjhtctn.finch_backend.mapper;

import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.model.Finch;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FinchMapper {

    public static FinchResponseDto toFinchResponse(Finch finch) {
        if (finch == null) return null;

        FinchResponseDto dto = toFinchResponseWithoutReplies(finch);

        if (finch.getReplies() != null && !finch.getReplies().isEmpty()) {
            List<FinchResponseDto> replies = finch.getReplies().stream()
                    .sorted(Comparator.comparing(Finch::getCreatedAt))
                    .map(FinchMapper::toFinchResponseWithoutReplies)
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

        if (finch.getParentFinch() != null)
            dto.setParentId(finch.getParentFinch().getId());

        dto.setReplyCount(finch.getReplies() != null ? finch.getReplies().size() : 0);
        return dto;
    }
}