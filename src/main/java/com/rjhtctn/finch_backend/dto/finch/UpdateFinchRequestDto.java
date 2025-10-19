package com.rjhtctn.finch_backend.dto.finch;

import lombok.Data;
import java.util.List;

@Data
public class UpdateFinchRequestDto {
    private String content;
    private List<String> deleteImageIds;
}