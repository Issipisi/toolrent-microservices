package com.example.users_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSystemDTO {
    private Long id = 0L;           // ID fijo para usuario sistema
    private String username = "SISTEMA";
    private String fullName = "Sistema Automático";
    private String[] roles = {"SYSTEM"};
}