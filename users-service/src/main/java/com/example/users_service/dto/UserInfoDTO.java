package com.example.users_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private String userId;          // ID de Keycloak (sub claim)
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String[] roles;
    private boolean emailVerified;
    private boolean enabled;
}
