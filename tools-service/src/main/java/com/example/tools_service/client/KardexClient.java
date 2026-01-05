package com.example.tools_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "kardex-service", path = "/api/kardex")
public interface KardexClient {

    @PostMapping("/movements/re-entry")
    void registerReEntry(
            @RequestParam("toolUnitId") Long toolUnitId,
            @RequestParam("toolGroupId") Long toolGroupId,
            @RequestParam(value = "repairCost", required = false) Double repairCost,
            @RequestParam(value = "notes", required = false) String notes);

    @PostMapping("/tool-creation")
    void registerToolCreation(
            @RequestParam Long toolUnitId,
            @RequestParam Long toolGroupId,
            @RequestParam String toolName);

    @PostMapping("/send-to-repair")
    void registerSendToRepair(
            @RequestParam Long toolUnitId,
            @RequestParam Long toolGroupId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String reason);

    @PostMapping("/reentry-from-repair")
    void registerReEntryFromRepair(
            @RequestParam Long toolUnitId,
            @RequestParam Long toolGroupId,
            @RequestParam(required = false) Double repairCost,
            @RequestParam boolean successful);

    @PostMapping("/retirement")
    void registerRetirement(
            @RequestParam Long toolUnitId,
            @RequestParam Long toolGroupId,
            @RequestParam String reason,
            @RequestParam(required = false) Long customerId);

    // Nuevos métodos agregados
    @PostMapping("/status-change")
    void registerStatusChange(
            @RequestParam Long toolUnitId,
            @RequestParam Long toolGroupId,
            @RequestParam String movementType,
            @RequestParam String notes);

    @PostMapping("/tool-retirement")
    void registerToolRetirement(
            @RequestParam Long toolUnitId,
            @RequestParam Long toolGroupId,
            @RequestParam String reason);

    // Método para registro de creación de múltiples herramientas
    @PostMapping("/tools-batch-creation")
    void registerToolsBatchCreation(
            @RequestParam Long toolGroupId,
            @RequestParam String toolGroupName,
            @RequestParam Integer quantity,
            @RequestParam String notes);
}