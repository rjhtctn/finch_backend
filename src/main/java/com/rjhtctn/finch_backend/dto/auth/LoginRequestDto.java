package com.rjhtctn.finch_backend.dto.auth;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String loginIdentifier;
    private String password;
}