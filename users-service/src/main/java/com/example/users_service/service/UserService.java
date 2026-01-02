package com.example.users_service.service;

import com.example.users_service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private static final String SYSTEM_USER_ID = "0";

    /**
     * Obtiene información del usuario actual desde el JWT
     */
    public UserInfoDTO getCurrentUserInfo() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
                throw new RuntimeException("No hay usuario autenticado con JWT");
            }

            Jwt jwt = (Jwt) authentication.getPrincipal();
            return extractUserInfoFromJwt(jwt);

        } catch (Exception e) {
            log.error("Error obteniendo información del usuario actual", e);
            throw new RuntimeException("Error obteniendo información del usuario: " + e.getMessage());
        }
    }

    /**
     * Valida un token JWT (básico - en producción usar librería completa)
     */
    public TokenValidationDTO validateToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return new TokenValidationDTO(false, null, null, null, "Token vacío");
            }

            // Token de sistema (para operaciones automáticas)
            if ("SYSTEM_TOKEN".equals(token)) {
                return new TokenValidationDTO(
                        true,
                        SYSTEM_USER_ID,
                        "SISTEMA",
                        new String[]{"SYSTEM"},
                        "Token de sistema válido"
                );
            }

            // En microservicios, la validación real la hace Spring Security
            // Este método solo extrae información básica
            // (En producción usarías una librería JWT para parsear)

            // Asumimos que si llega aquí, el token ya fue validado por Spring Security
            return new TokenValidationDTO(
                    true,
                    "user-id-placeholder",
                    "usuario@ejemplo.com",
                    new String[]{"EMPLOYEE"},
                    "Token aceptado por Spring Security"
            );

        } catch (Exception e) {
            log.error("Error validando token", e);
            return new TokenValidationDTO(false, null, null, null, "Token inválido: " + e.getMessage());
        }
    }

    /**
     * Obtiene información básica de usuario por ID
     * (En microservicios, normalmente no consultamos Keycloak Admin API)
     */
    public UserInfoDTO getUserById(String userId) {
        try {
            // Usuario SISTEMA
            if (SYSTEM_USER_ID.equals(userId) || "SISTEMA".equals(userId)) {
                return createSystemUser();
            }

            // En microservicios, normalmente NO consultamos Keycloak Admin API
            // porque requiere credenciales de admin y aumenta acoplamiento
            // Retornamos información básica o lanzamos excepción

            log.warn("Consultando usuario por ID no implementado completamente: {}", userId);

            UserInfoDTO userInfo = new UserInfoDTO();
            userInfo.setUserId(userId);
            userInfo.setUsername("user_" + userId);
            userInfo.setEmail(userId + "@toolrent.com");
            userInfo.setFullName("Usuario " + userId);
            userInfo.setRoles(new String[]{"EMPLOYEE"}); // Asumimos EMPLOYEE por defecto
            userInfo.setEnabled(true);
            userInfo.setEmailVerified(true);

            return userInfo;

        } catch (Exception e) {
            log.error("Error obteniendo usuario por ID: {}", userId, e);
            throw new RuntimeException("Error obteniendo usuario: " + e.getMessage());
        }
    }

    /**
     * Verifica si un usuario tiene un rol específico
     */
    public RoleCheckDTO checkUserRole(String userId, String role) {
        try {
            // Usuario SISTEMA siempre tiene rol SYSTEM
            if (SYSTEM_USER_ID.equals(userId) || "SISTEMA".equals(userId)) {
                return new RoleCheckDTO(userId, role, "SYSTEM".equals(role));
            }

            // Para otros usuarios, asumimos roles basados en ID
            // (En producción, esto vendría de una caché o base de datos)

            boolean hasRole = false;

            // Lógica de ejemplo:
            if (userId.endsWith("admin")) {
                hasRole = "ADMIN".equals(role) || "EMPLOYEE".equals(role);
            } else {
                hasRole = "EMPLOYEE".equals(role);
            }

            return new RoleCheckDTO(userId, role, hasRole);

        } catch (Exception e) {
            log.error("Error verificando rol", e);
            throw new RuntimeException("Error verificando rol: " + e.getMessage());
        }
    }

    /**
     * Obtiene usuario SISTEMA
     */
    public UserSystemDTO getSystemUser() {
        return new UserSystemDTO();
    }

    // ========== MÉTODOS PRIVADOS ==========

    private UserInfoDTO extractUserInfoFromJwt(Jwt jwt) {
        UserInfoDTO userInfo = new UserInfoDTO();

        userInfo.setUserId(jwt.getSubject());
        userInfo.setUsername(jwt.getClaimAsString("preferred_username"));
        userInfo.setEmail(jwt.getClaimAsString("email"));
        userInfo.setFirstName(jwt.getClaimAsString("given_name"));
        userInfo.setLastName(jwt.getClaimAsString("family_name"));
        userInfo.setFullName(jwt.getClaimAsString("name"));
        userInfo.setEmailVerified(Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified")));
        userInfo.setEnabled(true);

        // Extraer roles del JWT
        List<String> roles = extractRolesFromJwt(jwt);
        userInfo.setRoles(roles.toArray(new String[0]));

        return userInfo;
    }

    private List<String> extractRolesFromJwt(Jwt jwt) {
        Set<String> roles = new HashSet<>();

        // Roles de realm
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            @SuppressWarnings("unchecked")
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            if (realmRoles != null) {
                roles.addAll(realmRoles);
            }
        }

        // Roles de cliente específico
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null && resourceAccess.containsKey("toolrent-backend")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("toolrent-backend");
            if (clientAccess != null) {
                @SuppressWarnings("unchecked")
                List<String> clientRoles = (List<String>) clientAccess.get("roles");
                if (clientRoles != null) {
                    roles.addAll(clientRoles);
                }
            }
        }

        return new ArrayList<>(roles);
    }

    private UserInfoDTO createSystemUser() {
        UserInfoDTO systemUser = new UserInfoDTO();
        systemUser.setUserId(SYSTEM_USER_ID);
        systemUser.setUsername("SISTEMA");
        systemUser.setFullName("Sistema Automático");
        systemUser.setRoles(new String[]{"SYSTEM"});
        systemUser.setEnabled(true);
        systemUser.setEmailVerified(true);
        return systemUser;
    }

}