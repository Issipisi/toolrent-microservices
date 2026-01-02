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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KardexService {

    private final KardexRepository kardexRepository;
    private final ToolClient toolClient;
    private final CustomerClient customerClient;
    private final UserClient userClient; // Para obtener info de usuario

    /**
     * Registrar movimiento desde otros servicios
     */
    @Transactional
    public KardexResponseDTO registerMovement(KardexRequest request) {
        log.info("Registrando movimiento - Tipo: {}, Unidad: {}, Usuario: {}",
                request.getMovementType(), request.getToolUnitId(), request.getUserId());

        // Determinar usuario (si no viene, usar SYSTEM)
        Long userId = request.getUserId() != null ? request.getUserId() : SystemConstants.SYSTEM_USER_ID;

        // Obtener datos para denormalización
        ToolUnitModel toolUnit = getToolUnitInfo(request.getToolUnitId());
        CustomerModel customer = request.getCustomerId() != null ?
                getCustomerInfo(request.getCustomerId()) : null;
        UserModel user = getUserInfo(userId);

        // Crear movimiento
        KardexMovementEntity movement = new KardexMovementEntity();
        movement.setToolUnitId(request.getToolUnitId());
        movement.setToolGroupId(request.getToolGroupId() != null ?
                request.getToolGroupId() : toolUnit.getToolGroupId());
        movement.setCustomerId(request.getCustomerId());
        movement.setUserId(userId);
        movement.setMovementType(request.getMovementType());
        movement.setDetails(request.getDetails());

        // Campos denormalizados
        movement.setToolGroupName(toolUnit != null ? toolUnit.getToolGroupName() : "Desconocido");
        movement.setCustomerName(customer != null ? customer.getName() : "N/A");
        movement.setUserName(user.getFullName() != null ? user.getFullName() : user.getUsername());

        KardexMovementEntity saved = kardexRepository.save(movement);
        log.info("Movimiento registrado con ID: {}", saved.getId());

        return mapToDTO(saved);
    }

    /**
     * Registrar creación de herramienta (desde Tools Service)
     */
    @Transactional
    public KardexResponseDTO registerToolCreation(Long toolUnitId, Long toolGroupId, String toolName) {
        log.info("Registrando creación de herramienta - Unidad: {}, Grupo: {}", toolUnitId, toolGroupId);

        KardexRequest request = new KardexRequest();
        request.setMovementType(MovementType.REGISTRY);
        request.setToolUnitId(toolUnitId);
        request.setToolGroupId(toolGroupId);
        request.setDetails("Alta de nueva herramienta: " + toolName);
        request.setUserId(SystemConstants.SYSTEM_USER_ID);

        return registerMovement(request);
    }

    /**
     * Registrar préstamo (desde Loans Service)
     */
    @Transactional
    public KardexResponseDTO registerLoan(Long toolUnitId, Long toolGroupId, Long customerId, String customerName) {
        log.info("Registrando préstamo - Unidad: {}, Cliente: {}", toolUnitId, customerId);

        KardexRequest request = new KardexRequest();
        request.setMovementType(MovementType.LOAN);
        request.setToolUnitId(toolUnitId);
        request.setToolGroupId(toolGroupId);
        request.setCustomerId(customerId);
        request.setDetails("Préstamo a cliente: " + customerName);
        request.setUserId(SystemConstants.SYSTEM_USER_ID);

        return registerMovement(request);
    }

    /**
     * Registrar devolución (desde Loans Service)
     */
    @Transactional
    public KardexResponseDTO registerReturn(Long toolUnitId, Long toolGroupId, Long customerId,
                                            String customerName, boolean damaged, boolean overdue) {
        log.info("Registrando devolución - Unidad: {}, Cliente: {}", toolUnitId, customerId);

        String details = "Devolución de cliente: " + customerName;

        if (damaged && overdue) {
            details += " (con daño y atraso)";
        } else if (damaged) {
            details += " (con daño)";
        } else if (overdue) {
            details += " (con atraso)";
        }

        KardexRequest request = new KardexRequest();
        request.setMovementType(MovementType.RETURN);
        request.setToolUnitId(toolUnitId);
        request.setToolGroupId(toolGroupId);
        request.setCustomerId(customerId);
        request.setDetails(details);
        request.setUserId(SystemConstants.SYSTEM_USER_ID);

        return registerMovement(request);
    }

    /**
     * Registrar envío a reparación (desde Tools o Loans Service)
     */
    @Transactional
    public KardexResponseDTO registerSendToRepair(Long toolUnitId, Long toolGroupId, Long customerId, String reason) {
        log.info("Registrando envío a reparación - Unidad: {}", toolUnitId);

        String details = "Enviado a reparación";
        if (reason != null) {
            details += ": " + reason;
        }
        if (customerId != null) {
            details += " - Cliente: " + customerId;
        }

        KardexRequest request = new KardexRequest();
        request.setMovementType(MovementType.REPAIR);
        request.setToolUnitId(toolUnitId);
        request.setToolGroupId(toolGroupId);
        request.setCustomerId(customerId);
        request.setDetails(details);
        request.setUserId(SystemConstants.SYSTEM_USER_ID);

        return registerMovement(request);
    }

    /**
     * Registrar reingreso desde reparación (desde Tools Service)
     */
    @Transactional
    public KardexResponseDTO registerReEntryFromRepair(Long toolUnitId, Long toolGroupId,
                                                       Double repairCost, boolean successful) {
        log.info("Registrando reingreso desde reparación - Unidad: {}, Costo: {}", toolUnitId, repairCost);

        String details = successful ?
                "Reparación exitosa" :
                "Reparación fallida - Se mantiene en reparación";

        if (repairCost != null && repairCost > 0) {
            details += String.format(" (Costo reparación: $%.2f)", repairCost);
        }

        MovementType movementType = successful ? MovementType.RE_ENTRY : MovementType.REPAIR;

        KardexRequest request = new KardexRequest();
        request.setMovementType(movementType);
        request.setToolUnitId(toolUnitId);
        request.setToolGroupId(toolGroupId);
        request.setDetails(details);
        request.setUserId(SystemConstants.SYSTEM_USER_ID);

        return registerMovement(request);
    }

    /**
     * Registrar baja/retiro (desde Tools o Loans Service)
     */
    @Transactional
    public KardexResponseDTO registerRetirement(Long toolUnitId, Long toolGroupId, String reason, Long customerId) {
        log.info("Registrando retiro - Unidad: {}, Razón: {}", toolUnitId, reason);

        String details = "Baja de herramienta: " + reason;
        if (customerId != null) {
            details += " - Responsable cliente: " + customerId;
        }

        KardexRequest request = new KardexRequest();
        request.setMovementType(MovementType.RETIRE);
        request.setToolUnitId(toolUnitId);
        request.setToolGroupId(toolGroupId);
        request.setCustomerId(customerId);
        request.setDetails(details);
        request.setUserId(SystemConstants.SYSTEM_USER_ID);

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

    // ========== MÉTODOS PRIVADOS ==========

    private ToolUnitModel getToolUnitInfo(Long toolUnitId) {
        try {
            return toolClient.getToolUnit(toolUnitId);
        } catch (Exception e) {
            log.warn("No se pudo obtener información de la herramienta {}", toolUnitId, e);
            ToolUnitModel basic = new ToolUnitModel();
            basic.setId(toolUnitId);
            basic.setToolGroupId(0L);
            basic.setToolGroupName("Desconocido");
            return basic;
        }
    }

    private CustomerModel getCustomerInfo(Long customerId) {
        if (customerId == null) return null;

        try {
            return customerClient.getCustomer(customerId);
        } catch (Exception e) {
            log.warn("No se pudo obtener información del cliente {}", customerId, e);
            CustomerModel basic = new CustomerModel();
            basic.setId(customerId);
            basic.setName("Cliente " + customerId);
            return basic;
        }
    }

    private UserModel getUserInfo(Long userId) {
        // Usuario SISTEMA
        if (userId.equals(SystemConstants.SYSTEM_USER_ID)) {
            UserModel systemUser = new UserModel();
            systemUser.setId(SystemConstants.SYSTEM_USER_ID);
            systemUser.setUsername(SystemConstants.SYSTEM_USER_NAME);
            systemUser.setFullName(SystemConstants.SYSTEM_USER_FULL_NAME);
            return systemUser;
        }

        // Consultar Users Service
        try {
            return userClient.getUserById(String.valueOf(userId));
        } catch (Exception e) {
            log.warn("No se pudo obtener información del usuario {}", userId, e);
            UserModel basicUser = new UserModel();
            basicUser.setId(userId);
            basicUser.setUsername("Usuario_" + userId);
            basicUser.setFullName("Usuario " + userId);
            return basicUser;
        }
    }

    private KardexResponseDTO mapToDTO(KardexMovementEntity entity) {
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
}