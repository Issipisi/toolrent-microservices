package com.example.users_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleCheckDTO {
    private String userId;
    private String role;
    private boolean hasRole;
}
