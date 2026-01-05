package com.example.kardex_service.service;

import com.example.kardex_service.client.*;
import com.example.kardex_service.config.SystemConstants;
import com.example.kardex_service.dto.*;
import com.example.kardex_service.entity.KardexMovementEntity;
import com.example.kardex_service.entity.MovementType;
import com.example.kardex_service.model.*;
import com.example.kardex_service.repository.KardexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KardexService {

    private final KardexRepository kardexRepository;
    private final ToolClient toolClient;
    private final CustomerClient customerClient;
    private final UserClient userClient;

    // ========== MÉTODOS PARA REGISTRAR MOVIMIENTOS ==========

    // Métodos específicos para otros servicios

    // KardexService.java - Modificar registerMovement
    @Transactional
    public KardexResponseDTO registerMovement(KardexRequest request) {
        log.info("Registrando movimiento - Tipo: {}, Unidad: {}, Usuario: {}",
                request.getMovementType(), request.getToolUnitId(), request.getUserId());

        // Determinar usuario (si no viene, usar SYSTEM)
        Long userId = request.getUserId() != null ? request.getUserId() : SystemConstants.SYSTEM_USER_ID;

        // CustomerId puede ser null
        Long customerId = request.getCustomerId(); // Puede ser null

        // Obtener datos para denormalización
        ToolUnitModel toolUnit = null;
        CustomerModel customer = null;
        String toolGroupName = "Desconocido";
        String customerName = "N/A";

        try {
            toolUnit = toolClient.getToolUnit(request.getToolUnitId());
            if (toolUnit != null) {
                toolGroupName = toolUnit.getToolGroupName();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener información de la herramienta {}", request.getToolUnitId(), e);
        }

        // SOLO obtener info del cliente si customerId no es null
        if (customerId != null) {
            try {
                customer = customerClient.getCustomer(customerId);
                if (customer != null) {
                    customerName = customer.getName();
                }
            } catch (Exception e) {
                log.warn("No se pudo obtener información del cliente {}", customerId, e);
            }
        }

        // Crear movimiento
        KardexMovementEntity movement = new KardexMovementEntity();
        movement.setToolUnitId(request.getToolUnitId());
        movement.setToolGroupId(request.getToolGroupId() != null ?
                request.getToolGroupId() : (toolUnit != null ? toolUnit.getToolGroupId() : 0L));
        movement.setCustomerId(customerId);  // Puede ser null
        movement.setUserId(userId);
        movement.setMovementType(request.getMovementType());
        movement.setDetails(request.getDetails());
        movement.setToolGroupName(toolGroupName);
        movement.setCustomerName(customerName);
        movement.setUserName(getUserName(userId));

        KardexMovementEntity saved = kardexRepository.save(movement);
        log.info("Movimiento registrado con ID: {}", saved.getId());

        return mapToDTO(saved);
    }

    // Método auxiliar para obtener nombre de usuario
    private String getUserName(Long userId) {
        if (userId.equals(SystemConstants.SYSTEM_USER_ID)) {
            return "SISTEMA";
        }
        try {
            UserModel user = userClient.getUserById(String.valueOf(userId));
            return user.getFullName() != null ? user.getFullName() : user.getUsername();
        } catch (Exception e) {
            log.warn("No se pudo obtener información del usuario {}", userId, e);
            return "Usuario " + userId;
        }
    }

    @Transactional
    public KardexResponseDTO registerToolCreation(Long toolUnitId, Long toolGroupId, String toolName) {
        KardexRequest request = new KardexRequest();
        request.setMovementType(MovementType.REGISTRY);
        request.setToolUnitId(toolUnitId);
        request.setToolGroupId(toolGroupId);
        request.setDetails("Alta de nueva herramienta: " + toolName);
        return registerMovement(request);
    }

    @Transactional
    public KardexResponseDTO registerLoan(Long toolUnitId, Long toolGroupId, Long customerId, String customerName) {
        KardexRequest request = new KardexRequest();
        request.setMovementType(MovementType.LOAN);
        request.setToolUnitId(toolUnitId);
        request.setToolGroupId(toolGroupId);
        request.setCustomerId(customerId);
        request.setDetails("Préstamo a cliente: " + customerName);
        return registerMovement(request);
    }

    @Transactional
    public KardexResponseDTO registerReturn(Long toolUnitId, Long toolGroupId, Long customerId,
                                            String customerName, boolean damaged, boolean overdue) {
        String details = "Devolución de cliente: " + customerName;
        if (damaged && overdue) details += " (con daño y atraso)";
        else if (damaged) details += " (con daño)";
        else if (overdue) details += " (con atraso)";

        KardexRequest request = new KardexRequest();
        request.setMovementType(MovementType.RETURN);
        request.setToolUnitId(toolUnitId);
        request.setToolGroupId(toolGroupId);
        request.setCustomerId(customerId);
        request.setDetails(details);
        return registerMovement(request);
    }

    @Transactional
    public KardexResponseDTO registerSendToRepair(Long toolUnitId, Long toolGroupId, Long customerId, String reason) {
        String details = "Enviado a reparación" + (reason != null ? ": " + reason : "");
        if (customerId != null) details += " - Cliente: " + customerId;

        KardexRequest request = new KardexRequest();
        request.setMovementType(MovementType.REPAIR);
        request.setToolUnitId(toolUnitId);
        request.setToolGroupId(toolGroupId);
        request.setCustomerId(customerId);
        request.setDetails(details);
        return registerMovement(request);
    }

    @Transactional
    public KardexResponseDTO registerReEntryFromRepair(Long toolUnitId, Long toolGroupId,
                                                       Double repairCost, boolean successful) {
        String details = successful ? "Reparación exitosa" : "Reparación fallida";
        if (repairCost != null && repairCost > 0) {
            details += String.format(" (Costo: $%.2f)", repairCost);
        }

        MovementType movementType = successful ? MovementType.RE_ENTRY : MovementType.REPAIR;

        KardexRequest request = new KardexRequest();
        request.setMovementType(movementType);
        request.setToolUnitId(toolUnitId);
        request.setToolGroupId(toolGroupId);
        request.setDetails(details);
        return registerMovement(request);
    }

    @Transactional
    public KardexResponseDTO registerRetirement(Long toolUnitId, Long toolGroupId, String reason, Long customerId) {
        String details = "Baja de herramienta: " + reason;
        if (customerId != null) details += " - Cliente: " + customerId;

        KardexRequest request = new KardexRequest();
        request.setMovementType(MovementType.RETIRE);
        request.setToolUnitId(toolUnitId);
        request.setToolGroupId(toolGroupId);
        request.setCustomerId(customerId);
        request.setDetails(details);
        return registerMovement(request);
    }

    // ========== CONSULTAS ==========

    public List<KardexResponseDTO> getAllMovements() {
        return kardexRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<KardexResponseDTO> getMovementsByToolUnit(Long toolUnitId) {
        return kardexRepository.findByToolUnitId(toolUnitId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<KardexResponseDTO> getMovementsByToolGroup(Long toolGroupId) {
        return kardexRepository.findByToolGroupId(toolGroupId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<KardexResponseDTO> getMovementsByDateRange(LocalDateTime from, LocalDateTime to) {
        return kardexRepository.findByMovementDateBetween(from, to).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<KardexResponseDTO> getMovementsByToolUnitAndDateRange(Long toolUnitId, LocalDateTime from, LocalDateTime to) {
        return kardexRepository.findByToolUnitIdAndDateRange(toolUnitId, from, to).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<KardexResponseDTO> getMovementsByToolGroupAndDateRange(Long toolGroupId, LocalDateTime from, LocalDateTime to) {
        return kardexRepository.findByToolGroupIdAndDateRange(toolGroupId, from, to).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<KardexResponseDTO> getMovementsByMovementType(String movementType) {
        // Convertir string a enum si es necesario
        return kardexRepository.findByMovementType(movementType).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<KardexResponseDTO> getMovementsByCustomer(Long customerId) {
        return kardexRepository.findByCustomerId(customerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public KardexResponseDTO getLastMovementByToolUnit(Long toolUnitId) {
        KardexMovementEntity movement = kardexRepository.findTopByToolUnitIdOrderByMovementDateDesc(toolUnitId);
        return movement != null ? mapToDTO(movement) : null;
    }

    // ========== MÉTODOS PRIVADOS ==========

    private ToolUnitModel getToolUnitInfo(Long toolUnitId) {
        try {
            return toolClient.getToolUnit(toolUnitId);
        } catch (Exception e) {
            log.warn("No se pudo obtener información de la herramienta {}", toolUnitId, e);
            return createBasicToolUnit(toolUnitId);
        }
    }

    private CustomerModel getCustomerInfo(Long customerId) {
        if (customerId == null) return null;

        try {
            return customerClient.getCustomer(customerId);
        } catch (Exception e) {
            log.warn("No se pudo obtener información del cliente {}", customerId, e);
            return createBasicCustomer(customerId);
        }
    }

    private UserModel getUserInfo(Long userId) {
        // Usuario SISTEMA
        if (userId.equals(SystemConstants.SYSTEM_USER_ID)) {
            return createSystemUser();
        }

        // Consultar Users Service
        try {
            return userClient.getUserById(String.valueOf(userId));
        } catch (Exception e) {
            log.warn("No se pudo obtener información del usuario {}", userId, e);
            return createBasicUser(userId);
        }
    }

    // KardexService.java - Agregar validación
    private void validateMovementRequest(KardexRequest request) {
        if (request.getToolUnitId() == null) {
            throw new RuntimeException("toolUnitId es requerido");
        }

        if (request.getMovementType() == null) {
            throw new RuntimeException("movementType es requerido");
        }

        // Validar que si es préstamo o devolución, tenga customerId
        if ((request.getMovementType() == MovementType.LOAN ||
                request.getMovementType() == MovementType.RETURN ||
                request.getMovementType() == MovementType.REPAIR) &&
                request.getCustomerId() == null) {
            throw new RuntimeException("customerId es requerido para movimientos de tipo " +
                    request.getMovementType());
        }
    }

    // método específico para RE_ENTRY
    @Transactional
    public KardexResponseDTO registerReEntry(Long toolUnitId, Long toolGroupId,
                                             Double repairCost, String notes, Long userId) {
        log.info("Registrando re-ingreso - Unidad: {}, Grupo: {}", toolUnitId, toolGroupId);

        String details = "Re-ingreso desde reparación" +
                (repairCost != null ? String.format(" (Costo: $%.2f)", repairCost) : "") +
                (notes != null ? " - " + notes : "");

        KardexMovementEntity movement = new KardexMovementEntity();
        movement.setToolUnitId(toolUnitId);
        movement.setToolGroupId(toolGroupId);
        movement.setMovementType(MovementType.RE_ENTRY);
        movement.setDetails(details);
        movement.setUserId(userId != null ? userId : 0L); // SISTEMA por defecto

        KardexMovementEntity saved = kardexRepository.save(movement);
        log.info("Movimiento RE_ENTRY registrado con ID: {}", saved.getId());

        return mapToDTO(saved);
    }

    private KardexResponseDTO mapToDTO(KardexMovementEntity entity) {
        if (entity == null) return null;

        return new KardexResponseDTO(
                entity.getId(),
                entity.getToolUnitId(),
                entity.getToolGroupId(),
                entity.getToolGroupName(),
                entity.getCustomerId(),
                entity.getCustomerName(),
                entity.getUserId(),
                entity.getUserName(),
                entity.getMovementType().toString(),
                entity.getMovementDate(),
                entity.getDetails()
        );
    }

    // Métodos auxiliares para crear objetos básicos
    private ToolUnitModel createBasicToolUnit(Long toolUnitId) {
        ToolUnitModel basic = new ToolUnitModel();
        basic.setId(toolUnitId);
        basic.setToolGroupId(0L);
        basic.setToolGroupName("Desconocido");
        return basic;
    }

    private CustomerModel createBasicCustomer(Long customerId) {
        CustomerModel basic = new CustomerModel();
        basic.setId(customerId);
        basic.setName("Cliente " + customerId);
        return basic;
    }

    private UserModel createSystemUser() {
        UserModel systemUser = new UserModel();
        systemUser.setId(SystemConstants.SYSTEM_USER_ID);
        systemUser.setUsername(SystemConstants.SYSTEM_USER_NAME);
        systemUser.setFullName(SystemConstants.SYSTEM_USER_FULL_NAME);
        systemUser.setRole("SYSTEM");
        return systemUser;
    }

    private UserModel createBasicUser(Long userId) {
        UserModel basicUser = new UserModel();
        basicUser.setId(userId);
        basicUser.setUsername("Usuario_" + userId);
        basicUser.setFullName("Usuario " + userId);
        basicUser.setRole("EMPLOYEE");
        return basicUser;
    }
}