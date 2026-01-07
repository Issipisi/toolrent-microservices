package com.example.loans_service.client;

import com.example.loans_service.model.ToolUnitModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// En Loan Service - ToolClient.java
@FeignClient(name = "tools-service", path = "/api/tools/units")
public interface ToolClient {

    /*@GetMapping("/groups/{groupId}/available")
    ToolUnitModel getAvailableUnit(@PathVariable Long groupId);

    @GetMapping("/{unitId}/model")  // <- NUEVO endpoint específico
    ToolUnitModel getToolUnit(@PathVariable Long unitId);

    @PutMapping("/{unitId}/status")
    void updateStatus(@PathVariable Long unitId, @RequestParam String status);*/

    @GetMapping("/groups/{groupId}/available")
    ToolUnitModel getAvailableUnit(@PathVariable("groupId") Long toolGroupId);

    @GetMapping("/{id}")
    ToolUnitModel getToolUnit(@PathVariable("id") Long id);

    @PutMapping("/{id}/status")
    void updateStatus(@PathVariable("id") Long id,
                      @RequestParam String status,
                      @RequestParam(required = false) String userName);
}