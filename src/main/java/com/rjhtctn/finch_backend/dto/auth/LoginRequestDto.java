package com.rjhtctn.finch_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {

    @NotBlank(message = "Login identifier (username or email) cannot be blank")
    private String loginIdentifier;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}