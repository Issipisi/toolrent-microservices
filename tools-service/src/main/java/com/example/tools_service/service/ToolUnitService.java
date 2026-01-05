package com.example.tools_service.service;

import com.example.tools_service.client.KardexClient;
import com.example.tools_service.client.TariffClient;
import com.example.tools_service.dto.ToolUnitResponseDTO;
import com.example.tools_service.entity.ToolStatus;
import com.example.tools_service.entity.ToolUnitEntity;
import com.example.tools_service.entity.ToolGroupEntity; // Import necesario
import com.example.tools_service.model.TariffModel;
import com.example.tools_service.model.ToolUnitModel;
import com.example.tools_service.repository.ToolUnitRepository;
import com.example.tools_service.repository.ToolGroupRepository; // Import necesario
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToolUnitService {

    private final ToolUnitRepository toolUnitRepository;
    private final TariffClient tariffClient;
    private final KardexClient kardexClient;
    private final ToolGroupRepository toolGroupRepository;

    public ToolUnitModel getAvailableUnit(Long toolGroupId) {
        log.info("Buscando unidad disponible para grupo: {}", toolGroupId);

        ToolUnitEntity unit = toolUnitRepository
                .findFirstByToolGroupIdAndStatus(toolGroupId, ToolStatus.AVAILABLE)
                .orElseThrow(() -> new RuntimeException("No hay unidades disponibles para este grupo"));

        TariffModel tariff = getTariffFromService(unit.getToolGroup().getTariffId());
        return mapToModel(unit, tariff);
    }

    public ToolUnitModel getToolUnit(Long unitId) {
        log.info("Obteniendo información de unidad: {}", unitId);

        try {
            // Opción 1: Usar findById y cargar lazy
            ToolUnitEntity unit = toolUnitRepository.findById(unitId)
                    .orElseThrow(() -> new RuntimeException("Unidad no encontrada - ID: " + unitId));

            // Asegurarse de que toolGroup esté cargado
            if (unit.getToolGroup() == null) {
                throw new RuntimeException("Información de grupo no disponible para unidad: " + unitId);
            }

            // Obtener tarifa
            TariffModel tariff;
            try {
                tariff = tariffClient.getTariff(unit.getToolGroup().getTariffId());
                log.debug("Tarifa obtenida para unidad {}: {}", unitId, tariff);
            } catch (Exception e) {
                log.error("Error obteniendo tarifa {}: {}", unit.getToolGroup().getTariffId(), e.getMessage());
                // Crear tarifa por defecto para no romper el servicio
                tariff = new TariffModel(unit.getToolGroup().getTariffId(), 0.0, 0.0);
            }

            return mapToModel(unit, tariff);

        } catch (Exception e) {
            log.error("Error crítico en getToolUnit({}): {}", unitId, e.getMessage(), e);
            throw new RuntimeException("No se pudo obtener información de la herramienta: " + e.getMessage());
        }
    }

    // ===== MÉTODO PRINCIPAL: changeStatus con Kardex =====
    @Transactional
    public void updateStatus(Long unitId, String status) {
        ToolStatus newStatus;
        try {
            newStatus = ToolStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }

        ToolUnitEntity unit = toolUnitRepository.findByIdWithGroup(unitId)
                .orElseThrow(() -> new RuntimeException("Unit not found"));

        if (unit.getStatus() == ToolStatus.RETIRED) {
            throw new RuntimeException("Cannot change status of a retired unit");
        }

        if (unit.getStatus() == newStatus) {
            throw new RuntimeException("Unit is already in status: " + newStatus);
        }

        ToolStatus oldStatus = unit.getStatus();
        unit.setStatus(newStatus);
        ToolUnitEntity savedUnit = toolUnitRepository.save(unit);

        log.info("Unit {} changed status: {} -> {}", unitId, oldStatus, newStatus);

        // Registrar en kardex según el cambio
        registerKardexMovement(unit, oldStatus, newStatus);
    }

    public ToolUnitResponseDTO getUnitDetails(Long unitId) {
        ToolUnitEntity unit = toolUnitRepository.findByIdWithGroup(unitId)
                .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));

        TariffModel tariff = getTariffFromService(unit.getToolGroup().getTariffId());

        return new ToolUnitResponseDTO(
                unit.getId(),
                unit.getToolGroup().getId(),
                unit.getToolGroup().getName(),
                unit.getToolGroup().getCategory(),
                unit.getStatus(),
                unit.getToolGroup().getTariffId(),
                tariff.getDailyRentalRate(),
                tariff.getDailyFineRate(),
                unit.getToolGroup().getReplacementValue()
        );
    }

    public List<ToolUnitResponseDTO> getAllUnits() {
        // Usar el método que carga toolGroup con JOIN FETCH
        List<ToolUnitEntity> units = toolUnitRepository.findAllWithToolGroup();
        log.info("Total units with groups found: {}", units.size());

        return units.stream().map(unit -> {
            TariffModel tariff = getTariffFromService(unit.getToolGroup().getTariffId());

            log.debug("Processing unit {}: toolGroupName = {}",
                    unit.getId(), unit.getToolGroup().getName());

            return new ToolUnitResponseDTO(
                    unit.getId(),
                    unit.getToolGroup().getId(),
                    unit.getToolGroup().getName(),
                    unit.getToolGroup().getCategory(),
                    unit.getStatus(),
                    unit.getToolGroup().getTariffId(),
                    tariff.getDailyRentalRate(),
                    tariff.getDailyFineRate(),
                    unit.getToolGroup().getReplacementValue()
            );
        }).collect(Collectors.toList());
    }

    public long getAvailableStock(Long toolGroupId) {
        return toolUnitRepository.countByToolGroupIdAndStatus(toolGroupId, ToolStatus.AVAILABLE);
    }

    private void registerKardexMovement(ToolUnitEntity unit, ToolStatus oldStatus, ToolStatus newStatus) {
        try {
            Long toolGroupId = unit.getToolGroup().getId();
            String toolGroupName = unit.getToolGroup().getName();

            // Usar switch expression (Java 14+) - compacto y legible
            String movementType = switch (newStatus) {
                case IN_REPAIR -> "SEND_TO_REPAIR";
                case RETIRED -> "RETIREMENT";
                case AVAILABLE -> oldStatus == ToolStatus.IN_REPAIR ? "REPAIR_COMPLETION" : "AVAILABILITY";
                default -> "STATUS_CHANGE";
            };

            // Registrar movimiento básico
            kardexClient.registerStatusChange(
                    unit.getId(),
                    toolGroupId,
                    movementType,
                    "Status changed from " + oldStatus + " to " + newStatus
            );

            // Acciones específicas según el estado
            switch (newStatus) {
                case IN_REPAIR -> {
                    log.info("Kardex: Unit {} sent to repair", unit.getId());
                }
                case RETIRED -> {
                    // Reducir stock disponible (el stock se calcula dinámicamente)
                    ToolGroupEntity toolGroup = unit.getToolGroup();
                    log.info("Kardex: Unit {} retired, stock will be recalculated for group {}",
                            unit.getId(), toolGroupId);

                    // Registrar retiro específico
                    kardexClient.registerToolRetirement(
                            unit.getId(),
                            toolGroupId,
                            "Tool permanently retired from inventory"
                    );
                }
                case AVAILABLE -> {
                    if (oldStatus == ToolStatus.IN_REPAIR) {
                        log.info("Kardex: Unit {} returned from repair", unit.getId());
                    } else {
                        log.info("Kardex: Unit {} made available", unit.getId());
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error registering kardex movement for unit {}: {}",
                    unit.getId(), e.getMessage(), e);
            // No lanzamos excepción para no romper el flujo
        }
    }


    @Transactional
    public ToolUnitEntity repairResolution(Long unitId, boolean retire) {
        ToolUnitEntity unit = toolUnitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));

        if (unit.getStatus() != ToolStatus.IN_REPAIR) {
            throw new RuntimeException("La unidad no está en reparación");
        }

        if (retire) {
            unit.setStatus(ToolStatus.RETIRED);
            log.info("Unidad {} retirada después de reparación", unitId);

            // Registrar en Kardex
            try {
                kardexClient.registerRetirement(
                        unitId,
                        unit.getToolGroup().getId(),
                        "No reparable",
                        null
                );
            } catch (Exception e) {
                log.warn("Error registrando retiro en Kardex", e);
            }
        } else {
            unit.setStatus(ToolStatus.AVAILABLE);
            log.info("Unidad {} disponible después de reparación", unitId);

            // Registrar en Kardex
            try {
                kardexClient.registerReEntryFromRepair(
                        unitId,
                        unit.getToolGroup().getId(),
                        null, // No conocemos el costo aquí
                        true
                );
            } catch (Exception e) {
                log.warn("Error registrando reingreso en Kardex", e);
            }
        }

        return toolUnitRepository.save(unit);
    }

    // ========== MÉTODOS PRIVADOS ==========

    private ToolUnitModel mapToModel(ToolUnitEntity unit, TariffModel tariff) {
        return new ToolUnitModel(
                unit.getId(),
                unit.getStatus().toString(),
                unit.getToolGroup().getId(),
                unit.getToolGroup().getTariffId(),
                unit.getToolGroup().getName(),
                tariff.getDailyRentalRate(),
                tariff.getDailyFineRate(),
                unit.getToolGroup().getReplacementValue()
        );
    }

    private TariffModel getTariffFromService(Long tariffId) {
        try {
            return tariffClient.getTariff(tariffId);
        } catch (Exception e) {
            log.error("Error obteniendo tarifa {} de Tariff Service", tariffId, e);
            throw new RuntimeException("Error al obtener tarifa: " + e.getMessage());
        }
    }
}