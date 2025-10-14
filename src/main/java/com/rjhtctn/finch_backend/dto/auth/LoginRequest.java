package com.rjhtctn.finch_backend.dto.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String loginIdentifier;
    private String password;
}