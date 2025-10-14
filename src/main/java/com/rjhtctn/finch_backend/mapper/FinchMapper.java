package com.rjhtctn.finch_backend.mapper;

import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.model.Finch;

public class FinchMapper {

    public static FinchResponseDto toFinchResponse(Finch finch) {
        if (finch == null) {
            return null;
        }

        FinchResponseDto dto = new FinchResponseDto();
        dto.setId(finch.getId());
        dto.setContent(finch.getContent());
        dto.setCreatedAt(finch.getCreatedAt());
        dto.setAuthor(UserMapper.toUserResponse(finch.getUser()));

        return dto;
    }
}