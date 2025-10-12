package com.rjhtctn.finch_backend.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String loginIdentifier;
    private String password;
}