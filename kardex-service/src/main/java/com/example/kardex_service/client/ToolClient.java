package com.example.kardex_service.client;

import com.example.kardex_service.model.ToolUnitModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "tools-service", path = "/api/tools")
public interface ToolClient {

    @GetMapping("/units/{id}")
    ToolUnitModel getToolUnit(@PathVariable Long id);
}