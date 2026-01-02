package com.example.kardex_service.controller;

import com.example.kardex_service.dto.*;
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

    // ========== CONSULTAS ==========

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

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Kardex Service funcionando!");
    }
}