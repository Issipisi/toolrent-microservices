package com.example.users_service.controller;

import com.example.users_service.dto.*;
import com.example.users_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Endpoint para validar tokens (usado por otros servicios internos)
     * NO requiere autenticación
     */
    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationDTO> validateToken(@RequestHeader(value = "Authorization", required = false) String authorization) {
        // Si no hay token, validamos como inválido
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.ok(new TokenValidationDTO(false, null, null, null, "Token no proporcionado"));
        }

        String token = authorization.substring(7);
        TokenValidationDTO validation = userService.validateToken(token);
        return ResponseEntity.ok(validation);
    }

    /**
     * Obtiene información del usuario actual (autenticado)
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoDTO> getCurrentUser() {
        UserInfoDTO userInfo = userService.getCurrentUserInfo();
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Obtiene información de usuario por ID
     * Solo ADMIN puede consultar otros usuarios
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.claims['sub']")
    public ResponseEntity<UserInfoDTO> getUserById(@PathVariable String userId) {
        UserInfoDTO userInfo = userService.getUserById(userId);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Verifica si un usuario tiene un rol específico
     */
    @GetMapping("/{userId}/has-role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleCheckDTO> checkUserRole(
            @PathVariable String userId,
            @PathVariable String role) {
        RoleCheckDTO roleCheck = userService.checkUserRole(userId, role);
        return ResponseEntity.ok(roleCheck);
    }

    /**
     * Obtiene usuario SISTEMA (para operaciones automáticas)
     */
    @GetMapping("/system-user")
    public ResponseEntity<UserSystemDTO> getSystemUser() {
        UserSystemDTO systemUser = userService.getSystemUser();
        return ResponseEntity.ok(systemUser);
    }

    /**
     * Endpoint de prueba pública
     */
    @GetMapping("/public/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Users Service con Keycloak está funcionando! - Solo Resource Server");
    }

    /**
     * Endpoint solo para ADMIN
     */
    @GetMapping("/admin/only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Solo ADMIN puede ver esto");
    }

    /**
     * Endpoint para EMPLOYEE o ADMIN
     */
    @GetMapping("/employee/only")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<String> employeeOnly() {
        return ResponseEntity.ok("EMPLOYEE o ADMIN pueden ver esto");
    }

    /**
     * Endpoint para cualquiera autenticado
     */
    @GetMapping("/authenticated")
    public ResponseEntity<String> authenticated() {
        return ResponseEntity.ok("Cualquier usuario autenticado puede ver esto");
    }
}