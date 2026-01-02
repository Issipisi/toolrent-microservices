package com.example.tools_service.service;

import com.example.tools_service.client.KardexClient;
import com.example.tools_service.client.TariffClient;
import com.example.tools_service.dto.ToolUnitResponseDTO;
import com.example.tools_service.entity.ToolStatus;
import com.example.tools_service.entity.ToolUnitEntity;
import com.example.tools_service.model.TariffModel;
import com.example.tools_service.model.ToolUnitModel;
import com.example.tools_service.repository.ToolUnitRepository;
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

    // MÉTODO CRÍTICO para Loan Service
    public ToolUnitModel getAvailableUnit(Long toolGroupId) {
        log.info("Buscando unidad disponible para grupo: {}", toolGroupId);

        ToolUnitEntity unit = toolUnitRepository
                .findFirstByToolGroupIdAndStatus(toolGroupId, ToolStatus.AVAILABLE)
                .orElseThrow(() -> new RuntimeException("No hay unidades disponibles para este grupo"));

        TariffModel tariff = getTariffFromService(unit.getToolGroup().getTariffId());
        return mapToModel(unit, tariff);
    }

    // Otro método CRÍTICO para Loan Service
    public ToolUnitModel getToolUnit(Long unitId) {
        ToolUnitEntity unit = toolUnitRepository.findByIdWithGroup(unitId)
                .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));

        TariffModel tariff = getTariffFromService(unit.getToolGroup().getTariffId());
        return mapToModel(unit, tariff);
    }

    @Transactional
    public void updateStatus(Long unitId, String status) {
        ToolStatus newStatus;
        try {
            newStatus = ToolStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado inválido: " + status);
        }

        ToolUnitEntity unit = toolUnitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));

        // Validaciones
        if (unit.getStatus() == ToolStatus.RETIRED) {
            throw new RuntimeException("No se puede cambiar estado de una unidad retirada");
        }

        if (unit.getStatus() == newStatus) {
            throw new RuntimeException("La unidad ya está en estado: " + newStatus);
        }

        unit.setStatus(newStatus);
        toolUnitRepository.save(unit);
        log.info("Unidad {} cambió estado: {} -> {}", unitId, unit.getStatus(), newStatus);
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
        List<ToolUnitEntity> units = toolUnitRepository.findAll();

        return units.stream().map(unit -> {
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
        }).collect(Collectors.toList());
    }

    public long getAvailableStock(Long toolGroupId) {
        return toolUnitRepository.countByToolGroupIdAndStatus(toolGroupId, ToolStatus.AVAILABLE);
    }

    @Transactional
    public ToolUnitEntity repairResolution(Long unitId, boolean retire) {
        ToolUnitEntity unit = toolUnitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));

        if (unit.getStatus() != ToolStatus.IN_REPAIR) {
            throw new RuntimeException("La unidad no está en reparación");
        }

        ToolStatus target = retire ? ToolStatus.RETIRED : ToolStatus.AVAILABLE;
        unit.setStatus(target);

        return toolUnitRepository.save(unit);
    }

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

    // En ToolUnitService.java (tools-service)
    @Transactional
    public String sendToRepair(Long unitId, Double estimatedCost, String damageDescription) {
        ToolUnitEntity unit = toolUnitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));

        // Validar que esté en estado LOANED o AVAILABLE
        if (unit.getStatus() != ToolStatus.LOANED && unit.getStatus() != ToolStatus.AVAILABLE) {
            throw new RuntimeException("Solo herramientas prestadas o disponibles pueden enviarse a reparación");
        }

        // Cambiar estado
        unit.setStatus(ToolStatus.IN_REPAIR);
        toolUnitRepository.save(unit);

        // Registrar en Kardex
        try {
            String details = "Enviado a reparación";
            if (damageDescription != null) {
                details += ": " + damageDescription;
            }
            if (estimatedCost != null) {
                details += String.format(" (Costo estimado: $%.2f)", estimatedCost);
            }

            KardexClient.registerSendToRepair(
                    unitId,
                    unit.getToolGroup().getId(),
                    null, // No hay cliente asociado directamente
                    details
            );
        } catch (Exception e) {
            log.warn("Error registrando envío a reparación en Kardex", e);
        }

        return "Herramienta enviada a reparación";
    }

    @Transactional
    public ToolUnitEntity completeRepair(Long unitId, Double actualCost, boolean successful, String notes) {
        ToolUnitEntity unit = toolUnitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));

        if (unit.getStatus() != ToolStatus.IN_REPAIR) {
            throw new RuntimeException("La herramienta no está en reparación");
        }

        if (successful) {
            // Reparación exitosa - volver a AVAILABLE
            unit.setStatus(ToolStatus.AVAILABLE);

            // Registrar en Kardex como RE_ENTRY
            try {
                KardexClient.registerReEntryFromRepair(
                        unitId,
                        unit.getToolGroup().getId(),
                        actualCost,
                        true
                );
            } catch (Exception e) {
                log.warn("Error registrando reingreso en Kardex", e);
            }

            log.info("Reparación exitosa - Unidad {} disponible nuevamente", unitId);

        } else {
            // Reparación fallida - mantener en IN_REPAIR o RETIRAR
            // Depende de la política del negocio
            // Por ahora, mantenemos en IN_REPAIR para evaluación

            log.warn("Reparación fallida - Unidad {} requiere evaluación adicional", unitId);

            try {
                kardexClient.registerReEntryFromRepair(
                        unitId,
                        unit.getToolGroup().getId(),
                        actualCost,
                        false
                );
            } catch (Exception e) {
                log.warn("Error registrando falla de reparación en Kardex", e);
            }
        }

        ToolUnitEntity saved = toolUnitRepository.save(unit);

        return saved;
    }
}