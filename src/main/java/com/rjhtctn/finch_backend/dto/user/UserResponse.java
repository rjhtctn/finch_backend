package com.rjhtctn.finch_backend.dto.user;

import lombok.Data;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
}