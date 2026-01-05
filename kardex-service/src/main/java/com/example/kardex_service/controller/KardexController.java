package com.example.kardex_service.controller;

import com.example.kardex_service.dto.*;
import com.example.kardex_service.entity.MovementType;
import com.example.kardex_service.service.KardexService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/kardex")
@RequiredArgsConstructor
public class KardexController {

    private final KardexService kardexService;

    // ========== ENDPOINTS GENERALES ==========

    @PostMapping("/movements")
    public ResponseEntity<KardexResponseDTO> registerMovement(@RequestBody KardexRequest request) {
        KardexResponseDTO movement = kardexService.registerMovement(request);
        return ResponseEntity.ok(movement);
    }

    @GetMapping("/movements")
    public ResponseEntity<List<KardexResponseDTO>> getAllMovements() {
        List<KardexResponseDTO> movements = kardexService.getAllMovements();
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/movements/tool-unit/{toolUnitId}")
    public ResponseEntity<List<KardexResponseDTO>> getMovementsByToolUnit(@PathVariable Long toolUnitId) {
        List<KardexResponseDTO> movements = kardexService.getMovementsByToolUnit(toolUnitId);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/movements/tool-group/{toolGroupId}")
    public ResponseEntity<List<KardexResponseDTO>> getMovementsByToolGroup(@PathVariable Long toolGroupId) {
        List<KardexResponseDTO> movements = kardexService.getMovementsByToolGroup(toolGroupId);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/movements/date-range")
    public ResponseEntity<List<KardexResponseDTO>> getMovementsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDateTime startDate = from.atStartOfDay();
        LocalDateTime endDate = to.plusDays(1).atStartOfDay();

        List<KardexResponseDTO> movements = kardexService.getMovementsByDateRange(startDate, endDate);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/movements/tool-unit/{toolUnitId}/date-range")
    public ResponseEntity<List<KardexResponseDTO>> getMovementsByToolUnitAndDateRange(
            @PathVariable Long toolUnitId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDateTime startDate = from.atStartOfDay();
        LocalDateTime endDate = to.plusDays(1).atStartOfDay();

        List<KardexResponseDTO> movements = kardexService.getMovementsByToolUnitAndDateRange(toolUnitId, startDate, endDate);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/movements/tool-group/{toolGroupId}/date-range")
    public ResponseEntity<List<KardexResponseDTO>> getMovementsByToolGroupAndDateRange(
            @PathVariable Long toolGroupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDateTime startDate = from.atStartOfDay();
        LocalDateTime endDate = to.plusDays(1).atStartOfDay();

        List<KardexResponseDTO> movements = kardexService.getMovementsByToolGroupAndDateRange(toolGroupId, startDate, endDate);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/movements/type/{movementType}")
    public ResponseEntity<List<KardexResponseDTO>> getMovementsByType(@PathVariable String movementType) {
        List<KardexResponseDTO> movements = kardexService.getMovementsByMovementType(movementType);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/movements/customer/{customerId}")
    public ResponseEntity<List<KardexResponseDTO>> getMovementsByCustomer(@PathVariable Long customerId) {
        List<KardexResponseDTO> movements = kardexService.getMovementsByCustomer(customerId);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/movements/tool-unit/{toolUnitId}/last")
    public ResponseEntity<KardexResponseDTO> getLastMovementByToolUnit(@PathVariable Long toolUnitId) {
        KardexResponseDTO movement = kardexService.getLastMovementByToolUnit(toolUnitId);
        return movement != null ? ResponseEntity.ok(movement) : ResponseEntity.notFound().build();
    }

    // ========== ENDPOINTS ESPECÍFICOS PARA OTROS SERVICIOS ==========

    @PostMapping("/movements/tool-creation")
    public ResponseEntity<KardexResponseDTO> registerToolCreation(
            @RequestParam Long toolUnitId,
            @RequestParam Long toolGroupId,
            @RequestParam String toolName) {
        KardexResponseDTO movement = kardexService.registerToolCreation(toolUnitId, toolGroupId, toolName);
        return ResponseEntity.ok(movement);
    }

    @PostMapping("/movements/loan")
    public ResponseEntity<KardexResponseDTO> registerLoan(
            @RequestParam Long toolUnitId,
            @RequestParam Long toolGroupId,
            @RequestParam Long customerId,
            @RequestParam String customerName) {
        KardexResponseDTO movement = kardexService.registerLoan(toolUnitId, toolGroupId, customerId, customerName);
        return ResponseEntity.ok(movement);
    }

    @PostMapping("/movements/return")
    public ResponseEntity<KardexResponseDTO> registerReturn(
            @RequestParam Long toolUnitId,
            @RequestParam Long toolGroupId,
            @RequestParam Long customerId,
            @RequestParam String customerName,
            @RequestParam(defaultValue = "false") boolean damaged,
            @RequestParam(defaultValue = "false") boolean overdue) {
        KardexResponseDTO movement = kardexService.registerReturn(toolUnitId, toolGroupId, customerId,
                customerName, damaged, overdue);
        return ResponseEntity.ok(movement);
    }

    @PostMapping("/movements/send-to-repair")
    public ResponseEntity<KardexResponseDTO> registerSendToRepair(
            @RequestParam Long toolUnitId,
            @RequestParam Long toolGroupId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String reason) {
        KardexResponseDTO movement = kardexService.registerSendToRepair(toolUnitId, toolGroupId, customerId, reason);
        return ResponseEntity.ok(movement);
    }

    @PostMapping("/movements/reentry-from-repair")
    public ResponseEntity<KardexResponseDTO> registerReEntryFromRepair(
            @RequestParam Long toolUnitId,
            @RequestParam Long toolGroupId,
            @RequestParam(required = false) Double repairCost,
            @RequestParam(defaultValue = "true") boolean successful) {
        KardexResponseDTO movement = kardexService.registerReEntryFromRepair(toolUnitId, toolGroupId, repairCost, successful);
        return ResponseEntity.ok(movement);
    }

    @PostMapping("/movements/retirement")
    public ResponseEntity<KardexResponseDTO> registerRetirement(
            @RequestParam Long toolUnitId,
            @RequestParam Long toolGroupId,
            @RequestParam String reason,
            @RequestParam(required = false) Long customerId) {
        KardexResponseDTO movement = kardexService.registerRetirement(toolUnitId, toolGroupId, reason, customerId);
        return ResponseEntity.ok(movement);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Kardex Service funcionando correctamente!");
    }

    // KardexController.java - Solo modificar el endpoint de RE_ENTRY
    @PostMapping("/movements/re-entry")
    public ResponseEntity<KardexResponseDTO> registerReEntry(
            @RequestParam Long toolUnitId,
            @RequestParam Long toolGroupId,
            @RequestParam(required = false) Double repairCost,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) Long userId) {

        KardexResponseDTO movement = kardexService.registerReEntry(
                toolUnitId, toolGroupId, repairCost, notes, userId);
        return ResponseEntity.ok(movement);
    }

    @PostMapping("/status-change")
    public ResponseEntity<Void> registerStatusChange(
            @RequestParam Long toolUnitId,
            @RequestParam Long toolGroupId,
            @RequestParam String movementType,
            @RequestParam String notes) {

        return ResponseEntity.ok().build();
    }

    @PostMapping("/tool-retirement")
    public ResponseEntity<Void> registerToolRetirement(
            @RequestParam Long toolUnitId,
            @RequestParam Long toolGroupId,
            @RequestParam String reason) {

        return ResponseEntity.ok().build();
    }

    @PostMapping("/tools-batch-creation")
    public ResponseEntity<Void> registerToolsBatchCreation(
            @RequestParam Long toolGroupId,
            @RequestParam String toolGroupName,
            @RequestParam Integer quantity,
            @RequestParam String notes) {

        return ResponseEntity.ok().build();
    }
}