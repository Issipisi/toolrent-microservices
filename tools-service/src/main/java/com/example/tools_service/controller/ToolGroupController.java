package com.example.tools_service.controller;

import com.example.tools_service.dto.ToolGroupRequestDTO;
import com.example.tools_service.dto.ToolGroupResponseDTO;
import com.example.tools_service.service.ToolGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tools/groups")
@RequiredArgsConstructor
public class ToolGroupController {

    private final ToolGroupService toolGroupService;

    @PostMapping
    public ResponseEntity<ToolGroupResponseDTO> createToolGroup(
            @Valid @RequestBody ToolGroupRequestDTO request) {
        ToolGroupResponseDTO created = toolGroupService.createToolGroup(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<ToolGroupResponseDTO>> getAllToolGroups() {
        List<ToolGroupResponseDTO> groups = toolGroupService.getAllToolGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ToolGroupResponseDTO> getToolGroup(@PathVariable Long id) {
        ToolGroupResponseDTO group = toolGroupService.getToolGroup(id);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/available")
    public ResponseEntity<List<ToolGroupResponseDTO>> getAvailableToolGroups() {
        List<ToolGroupResponseDTO> groups = toolGroupService.getAvailableToolGroups();
        return ResponseEntity.ok(groups);
    }

    @PutMapping("/{id}/tariff")
    public ResponseEntity<ToolGroupResponseDTO> updateTariff(
            @PathVariable Long id,
            @RequestParam Double dailyRentalRate,
            @RequestParam Double dailyFineRate) {
        ToolGroupResponseDTO updated = toolGroupService.updateTariff(id, dailyRentalRate, dailyFineRate);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/replacement-value")
    public ResponseEntity<ToolGroupResponseDTO> updateReplacementValue(
            @PathVariable Long id,
            @RequestParam Double replacementValue) {
        ToolGroupResponseDTO updated = toolGroupService.updateReplacementValue(id, replacementValue);
        return ResponseEntity.ok(updated);
    }
}