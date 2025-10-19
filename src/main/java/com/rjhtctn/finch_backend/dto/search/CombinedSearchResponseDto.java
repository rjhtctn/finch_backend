package com.rjhtctn.finch_backend.dto.search;

import com.rjhtctn.finch_backend.dto.finch.FinchResponseDto;
import com.rjhtctn.finch_backend.dto.user.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class CombinedSearchResponseDto {
    private List<UserResponseDto> users;
    private List<FinchResponseDto> finches;
}