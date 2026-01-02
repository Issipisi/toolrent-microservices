package com.example.tools_service.controller;

import com.example.tools_service.dto.ToolUnitResponseDTO;
import com.example.tools_service.entity.ToolUnitEntity;
import com.example.tools_service.model.ToolUnitModel;
import com.example.tools_service.service.ToolUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tools/units")
@RequiredArgsConstructor
public class ToolUnitController {

    private final ToolUnitService toolUnitService;

    // ENDPOINT CRÍTICO para Loan Service
    @GetMapping("/{id}")
    public ResponseEntity<ToolUnitResponseDTO> getUnitDetails(@PathVariable Long id) {
        ToolUnitResponseDTO unit = toolUnitService.getUnitDetails(id);
        return ResponseEntity.ok(unit);
    }

    // ENDPOINT CRÍTICO para Loan Service - para reservar unidad
    @GetMapping("/groups/{groupId}/available")
    public ResponseEntity<ToolUnitModel> getAvailableUnit(@PathVariable Long groupId) {
        ToolUnitModel unit = toolUnitService.getAvailableUnit(groupId);
        return ResponseEntity.ok(unit);
    }

    // ENDPOINT CRÍTICO para Loan Service - para cambiar estado
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        toolUnitService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/groups/{groupId}/stock")
    public ResponseEntity<Long> getAvailableStock(@PathVariable Long groupId) {
        long stock = toolUnitService.getAvailableStock(groupId);
        return ResponseEntity.ok(stock);
    }

    @GetMapping
    public ResponseEntity<List<ToolUnitResponseDTO>> getAllUnits() {
        List<ToolUnitResponseDTO> units = toolUnitService.getAllUnits();
        return ResponseEntity.ok(units);
    }

    @PutMapping("/{unitId}/repair-resolution")
    public ResponseEntity<ToolUnitEntity> resolveRepair(
            @PathVariable Long unitId,
            @RequestParam boolean retire) {
        ToolUnitEntity updated = toolUnitService.repairResolution(unitId, retire);
        return ResponseEntity.ok(updated);
    }

    // En ToolUnitController.java (tools-service)
    @PutMapping("/{unitId}/repair")
    public ResponseEntity<String> sendToRepair(
            @PathVariable Long unitId,
            @RequestParam(required = false) Double estimatedCost,
            @RequestParam(required = false) String damageDescription) {

        String result = toolUnitService.sendToRepair(unitId, estimatedCost, damageDescription);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{unitId}/complete-repair")
    public ResponseEntity<ToolUnitEntity> completeRepair(
            @PathVariable Long unitId,
            @RequestParam Double actualCost,
            @RequestParam boolean successful,
            @RequestParam(required = false) String notes) {

        ToolUnitEntity updated = toolUnitService.completeRepair(unitId, actualCost, successful, notes);
        return ResponseEntity.ok(updated);
    }
}