package com.example.kardex_service.client;

import com.example.kardex_service.model.UserModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// UserClient.java
@FeignClient(
        name = "users-service",
        contextId = "kardexUserClient",  // ← CONTEXT ID ÚNICO
        path = "/api/users",
        fallback = UserClientFallback.class
)
public interface UserClient {
    @GetMapping("/{userId}")
    UserModel getUserById(@PathVariable String userId);

    @GetMapping("/system-user")
    UserModel getSystemUser();
}