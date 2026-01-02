package com.example.kardex_service.client;

import com.example.kardex_service.model.UserModel;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserModel getUserById(String userId) {
        // Si Users Service no está disponible, crear usuario básico
        UserModel user = new UserModel();
        user.setId(Long.parseLong(userId));
        user.setUsername("Usuario_" + userId);
        user.setFullName("Usuario " + userId);
        user.setRole("EMPLOYEE");
        return user;
    }

    @Override
    public UserModel getSystemUser() {
        UserModel systemUser = new UserModel();
        systemUser.setId(0L);
        systemUser.setUsername("SISTEMA");
        systemUser.setFullName("Sistema Automático");
        systemUser.setRole("SYSTEM");
        return systemUser;
    }
}