package com.example.users_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationDTO {
    private boolean valid;
    private String userId;
    private String username;
    private String[] roles;
    private String message;
}
