package com.example.kardex_service.client;

import com.example.kardex_service.model.ToolUnitModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// ToolClient.java
@FeignClient(
        name = "tools-service",
        contextId = "kardexToolClient",  // ← CONTEXT ID ÚNICO
        path = "/api/tools/units"
)
public interface ToolClient {
    @GetMapping("/{unitId}")
    ToolUnitModel getToolUnit(@PathVariable Long unitId);
}