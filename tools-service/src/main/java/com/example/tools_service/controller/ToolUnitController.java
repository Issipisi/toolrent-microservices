package com.example.tools_service.controller;

import com.example.tools_service.dto.ToolUnitResponseDTO;
import com.example.tools_service.entity.ToolUnitEntity;
import com.example.tools_service.model.ToolUnitModel;
import com.example.tools_service.service.ToolUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Mantener solo lo esencial
@RestController
@RequestMapping("/api/tools/units")
@RequiredArgsConstructor
public class ToolUnitController {

    private final ToolUnitService toolUnitService;

    // CRÍTICO para Loan Service
    @GetMapping("/{id}")
    public ResponseEntity<ToolUnitResponseDTO> getUnitDetails(@PathVariable Long id) {
        ToolUnitResponseDTO unit = toolUnitService.getUnitDetails(id);
        return ResponseEntity.ok(unit);
    }

    // CRÍTICO para Loan Service
    @GetMapping("/{id}/model")
    public ResponseEntity<ToolUnitModel> getToolUnitModel(@PathVariable Long id) {
        ToolUnitModel unit = toolUnitService.getToolUnit(id);
        return ResponseEntity.ok(unit);
    }

    // CRÍTICO para Loan Service - para reservar unidad
    @GetMapping("/groups/{groupId}/available")
    public ResponseEntity<ToolUnitModel> getAvailableUnit(@PathVariable Long groupId) {
        ToolUnitModel unit = toolUnitService.getAvailableUnit(groupId);
        return ResponseEntity.ok(unit);
    }

    // CRÍTICO para Loan Service - para cambiar estado
    @PutMapping("/{id}/status")
    public void updateStatus(@PathVariable Long id,
                             @RequestParam String status,
                             @RequestParam(required = false) String userName) {
        toolUnitService.updateStatus(id, status, userName);
    }

    @PutMapping("/{id}/repair-resolution")
    public ToolUnitEntity repairResolution(@PathVariable Long id,
                                           @RequestParam boolean retire,
                                           @RequestParam(required = false) String userName) {
        return toolUnitService.repairResolution(id, retire, userName);
    }

    @GetMapping
    public ResponseEntity<List<ToolUnitResponseDTO>> getAllUnits() {
        List<ToolUnitResponseDTO> units = toolUnitService.getAllUnits();
        return ResponseEntity.ok(units);
    }

    // Opcional - para reportes
    @GetMapping("/groups/{groupId}/stock")
    public ResponseEntity<Long> getAvailableStock(@PathVariable Long groupId) {
        long stock = toolUnitService.getAvailableStock(groupId);
        return ResponseEntity.ok(stock);
    }


    @PutMapping("/{id}/resolve-repair")
    public ResponseEntity<Void> resolveRepair(
            @PathVariable Long id,
            @RequestParam boolean retire,
            @RequestParam(required = false) String userName) {
        toolUnitService.repairResolution(id, retire, userName);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/retire-from-repair")
    public ResponseEntity<Void> retireFromRepair(@PathVariable Long id, @RequestParam(required = false) String userName) {
        toolUnitService.repairResolution(id, true, userName); // true = retire
        return ResponseEntity.ok().build();
    }

}