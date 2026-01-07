package com.example.tools_service.service;

import com.example.tools_service.client.KardexClient;
import com.example.tools_service.client.TariffClient;
import com.example.tools_service.dto.ToolGroupRequestDTO;
import com.example.tools_service.dto.ToolGroupResponseDTO;
import com.example.tools_service.entity.ToolGroupEntity;
import com.example.tools_service.entity.ToolStatus;
import com.example.tools_service.entity.ToolUnitEntity;
import com.example.tools_service.model.TariffModel;
import com.example.tools_service.repository.ToolGroupRepository;
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
public class ToolGroupService {

    private final ToolGroupRepository toolGroupRepository;
    private final ToolUnitRepository toolUnitRepository;
    private final TariffClient tariffClient;
    private final KardexClient kardexClient; // NUEVO: Kardex Client

    @Transactional
    public ToolGroupResponseDTO createToolGroup(ToolGroupRequestDTO request, String userName) {
        log.info("Creando grupo de herramientas: {}", request.getName());

        // 1. Validar nombre único
        if (toolGroupRepository.existsByName(request.getName())) {
            throw new RuntimeException("Ya existe un grupo de herramientas con ese nombre");
        }

        // 2. Crear tarifa en Tariff Service
        TariffModel tariff;
        try {
            tariff = tariffClient.createTariff(
                    request.getDailyRentalRate(),
                    request.getDailyFineRate()
            );
            log.info("Tarifa creada en Tariff Service: ID {}", tariff.getId());
        } catch (Exception e) {
            log.error("Error creando tarifa en Tariff Service", e);
            throw new RuntimeException("Error al crear tarifa: " + e.getMessage());
        }

        // 3. Crear grupo con tariffId
        ToolGroupEntity group = new ToolGroupEntity();
        group.setName(request.getName());
        group.setCategory(request.getCategory());
        group.setReplacementValue(request.getReplacementValue());
        group.setTariffId(tariff.getId());

        // 4. Crear unidades iniciales
        for (int i = 0; i < request.getInitialStock(); i++) {
            ToolUnitEntity unit = new ToolUnitEntity();
            unit.setToolGroup(group);
            unit.setStatus(ToolStatus.AVAILABLE);
            group.getUnits().add(unit);
        }

        // 5. Guardar el grupo (esto guarda también las unidades por cascade)
        ToolGroupEntity saved = toolGroupRepository.save(group);
        log.info("Grupo de herramientas creado: ID {}, Unidades: {}",
                saved.getId(), saved.getUnits().size());


        // ===== REGISTRAR EN KARDEX: CREACIÓN DE HERRAMIENTAS =====
        try {
            // Registrar movimiento de creación del grupo
            kardexClient.registerToolsBatchCreation(
                    saved.getId(),
                    saved.getName(),
                    saved.getUnits().size(),
                    userName != null ? userName : "Usuario"
            );
            log.info("Kardex: Tool group {} created with {} units", saved.getId(), saved.getUnits().size());

        } catch (Exception e) {
            log.error("Error registering tool group creation in kardex: {}", e.getMessage(), e);
        }

        return mapToDTO(saved, tariff);
    }

    public ToolGroupResponseDTO getToolGroup(Long id) {
        ToolGroupEntity group = toolGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo de herramientas no encontrado"));

        TariffModel tariff = getTariffFromService(group.getTariffId());
        return mapToDTO(group, tariff);
    }

    public List<ToolGroupResponseDTO> getAllToolGroups() {
        return toolGroupRepository.findAll().stream()
                .map(group -> {
                    TariffModel tariff = getTariffFromService(group.getTariffId());
                    return mapToDTO(group, tariff);
                })
                .collect(Collectors.toList());
    }

    public List<ToolGroupResponseDTO> getAvailableToolGroups() {
        return toolGroupRepository.findAll().stream()
                .filter(group -> group.getUnits().stream()
                        .anyMatch(unit -> unit.getStatus() == ToolStatus.AVAILABLE))
                .map(group -> {
                    TariffModel tariff = getTariffFromService(group.getTariffId());
                    return mapToDTO(group, tariff);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ToolGroupResponseDTO updateTariff(Long groupId, Double dailyRentalRate, Double dailyFineRate) {
        ToolGroupEntity group = toolGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo de herramientas no encontrado"));

        try {
            TariffModel tariff = tariffClient.updateTariff(
                    group.getTariffId(),
                    dailyRentalRate,
                    dailyFineRate
            );
            log.info("Tarifa actualizada en Tariff Service: ID {}", tariff.getId());
            return mapToDTO(group, tariff);
        } catch (Exception e) {
            log.error("Error actualizando tarifa en Tariff Service", e);
            throw new RuntimeException("Error al actualizar tarifa: " + e.getMessage());
        }
    }

    @Transactional
    public ToolGroupResponseDTO updateReplacementValue(Long groupId, Double replacementValue) {
        ToolGroupEntity group = toolGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo de herramientas no encontrado"));

        group.setReplacementValue(replacementValue);
        ToolGroupEntity saved = toolGroupRepository.save(group);

        TariffModel tariff = getTariffFromService(saved.getTariffId());
        return mapToDTO(saved, tariff);
    }

    // ========== MÉTODOS PARA MANEJO DE REPARACIONES ==========

    /**
     * Enviar herramienta a reparación

    @Transactional
    public void sendToRepair(Long unitId, String reason, Long customerId) {
        ToolUnitEntity unit = toolUnitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));

        // Validar que pueda ir a reparación
        if (unit.getStatus() != ToolStatus.AVAILABLE && unit.getStatus() != ToolStatus.LOANED) {
            throw new RuntimeException("La herramienta no puede enviarse a reparación en su estado actual");
        }

        // Cambiar estado
        unit.setStatus(ToolStatus.IN_REPAIR);
        toolUnitRepository.save(unit);
        log.info("Unidad {} enviada a reparación", unitId);

        // Registrar en Kardex
        try {
            kardexClient.registerSendToRepair(
                    unitId,
                    unit.getToolGroup().getId(),
                    customerId,
                    reason
            );
            log.info("Movimiento de reparación registrado en Kardex");
        } catch (Exception e) {
            log.warn("Error registrando reparación en Kardex", e);
        }
    }*/

    /**
     * Completar reparación de herramienta

    @Transactional
    public void completeRepair(Long unitId, Double repairCost, boolean successful, String notes) {
        ToolUnitEntity unit = toolUnitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));

        if (unit.getStatus() != ToolStatus.IN_REPAIR) {
            throw new RuntimeException("La herramienta no está en reparación");
        }

        if (successful) {
            // Reparación exitosa - volver a AVAILABLE
            unit.setStatus(ToolStatus.AVAILABLE);
            log.info("Reparación exitosa - Unidad {} disponible", unitId);

            // Registrar RE_ENTRY en Kardex
            try {
                // Llamar al nuevo endpoint de re-ingreso
                kardexClient.registerReEntry(
                        unitId,
                        unit.getToolGroup().getId(),
                        repairCost,
                        "Reparación exitosa: " + (notes != null ? notes : "")
                );
                log.info("Movimiento RE_ENTRY registrado en Kardex");
            } catch (Exception e) {
                log.warn("Error registrando RE_ENTRY en Kardex", e);
            }
        } else {
            // Reparación fallida - mantener en reparación para evaluación
            log.warn("Reparación fallida - Unidad {} requiere evaluación", unitId);
            // Podemos registrar también en Kardex como reparación fallida
            try {
                kardexClient.registerSendToRepair(
                        unitId,
                        unit.getToolGroup().getId(),
                        null,
                        "Reparación fallida: " + (notes != null ? notes : "Requiere evaluación adicional")
                );
            } catch (Exception e) {
                log.warn("Error registrando reparación fallida en Kardex", e);
            }
        }

        toolUnitRepository.save(unit);
    }*/

    /**
     * Retirar herramienta (baja definitiva)
     */
    @Transactional
    public void retireTool(Long unitId, String reason, Long customerId) {
        ToolUnitEntity unit = toolUnitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));

        // Cambiar estado a RETIRED
        unit.setStatus(ToolStatus.RETIRED);
        toolUnitRepository.save(unit);
        log.info("Unidad {} retirada: {}", unitId, reason);

        // Registrar en Kardex
        try {
            kardexClient.registerRetirement(
                    unitId,
                    unit.getToolGroup().getId(),
                    reason,
                    customerId
            );
            log.info("Movimiento de retiro registrado en Kardex");
        } catch (Exception e) {
            log.warn("Error registrando retiro en Kardex", e);
        }
    }

    // ========== MÉTODOS PRIVADOS ==========

    private ToolGroupResponseDTO mapToDTO(ToolGroupEntity entity, TariffModel tariff) {
        long availableCount = entity.getUnits().stream()
                .filter(unit -> unit.getStatus() == ToolStatus.AVAILABLE)
                .count();

        // Corregir: usar el constructor completo con totalUnits
        return new ToolGroupResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getCategory(),
                entity.getReplacementValue(),
                entity.getTariffId(),
                tariff.getDailyRentalRate(),
                tariff.getDailyFineRate(),
                availableCount,
                (long) entity.getUnits().size() // Total de unidades
        );
    }

    private TariffModel getTariffFromService(Long tariffId) {
        try {
            return tariffClient.getTariff(tariffId);
        } catch (Exception e) {
            log.error("Error obteniendo tarifa {} de Tariff Service", tariffId, e);
            // Retornar tarifa por defecto para no romper el servicio
            return new TariffModel(tariffId, 0.0, 0.0);
        }
    }

}