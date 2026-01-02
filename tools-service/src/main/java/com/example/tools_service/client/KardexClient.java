package com.example.tools_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "kardex-service", path = "/api/kardex/movements")
public interface KardexClient {

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
}