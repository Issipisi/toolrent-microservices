package com.example.tools_service.service;

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

    @Transactional
    public ToolGroupResponseDTO createToolGroup(ToolGroupRequestDTO request) {
        // Validar nombre único
        if (toolGroupRepository.existsByName(request.getName())) {
            throw new RuntimeException("Ya existe un grupo de herramientas con ese nombre");
        }

        // 1. Crear tarifa en Tariff Service
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

        // 2. Crear grupo con tariffId
        ToolGroupEntity group = new ToolGroupEntity();
        group.setName(request.getName());
        group.setCategory(request.getCategory());
        group.setReplacementValue(request.getReplacementValue());
        group.setTariffId(tariff.getId());

        // 3. Crear unidades iniciales
        for (int i = 0; i < request.getInitialStock(); i++) {
            ToolUnitEntity unit = new ToolUnitEntity();
            unit.setToolGroup(group);
            unit.setStatus(ToolStatus.AVAILABLE);
            group.getUnits().add(unit);
        }

        ToolGroupEntity saved = toolGroupRepository.save(group);
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

    // MÉTODO FALTANTE: getAvailableToolGroups
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

    private ToolGroupResponseDTO mapToDTO(ToolGroupEntity entity, TariffModel tariff) {
        long availableCount = entity.getUnits().stream()
                .filter(unit -> unit.getStatus() == ToolStatus.AVAILABLE)
                .count();

        return new ToolGroupResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getCategory(),
                entity.getReplacementValue(),
                entity.getTariffId(),
                tariff.getDailyRentalRate(),
                tariff.getDailyFineRate(),
                availableCount
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