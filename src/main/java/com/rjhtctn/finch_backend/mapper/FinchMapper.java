package com.rjhtctn.finch_backend.mapper;

import com.rjhtctn.finch_backend.dto.response.FinchResponse;
import com.rjhtctn.finch_backend.model.Finch;

public class FinchMapper {

    public static FinchResponse toFinchResponse(Finch finch) {
        if (finch == null) {
            return null;
        }

        FinchResponse dto = new FinchResponse();
        dto.setId(finch.getId());
        dto.setContent(finch.getContent());
        dto.setCreatedAt(finch.getCreatedAt());
        dto.setAuthor(UserMapper.toUserResponse(finch.getUser()));

        return dto;
    }
}