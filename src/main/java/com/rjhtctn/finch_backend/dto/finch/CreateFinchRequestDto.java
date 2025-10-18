package com.rjhtctn.finch_backend.dto.finch;

import io.swagger.v3.oas.annotations.media.Schema;
    import lombok.Data;

@Data
@Schema(name = "CreateFinchRequest", description = "Form data for Finch creation")
public class CreateFinchRequestDto {
    @Schema(description = "Text content of the Finch (optional)", example = "Bugün hava güzel 😎")
    private String content;
}