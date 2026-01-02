package com.example.loans_service.client;

import com.example.loans_service.model.ToolUnitModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "tools-service", path = "/api/tools")
public interface ToolClient {

    @GetMapping("/units/groups/{groupId}/available")
    ToolUnitModel getAvailableUnit(@PathVariable Long groupId);

    @GetMapping("/units/{unitId}")
    ToolUnitModel getToolUnit(@PathVariable Long unitId);

    @PutMapping("/units/{unitId}/status")
    void updateStatus(@PathVariable Long unitId, @RequestParam String status);
}