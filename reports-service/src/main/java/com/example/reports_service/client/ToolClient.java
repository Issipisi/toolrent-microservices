package com.example.reports_service.client;

import com.example.reports_service.model.ToolGroupModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "tools-service", path = "/api/tools")
public interface ToolClient {

    @GetMapping("/groups")
    List<ToolGroupModel> getAllToolGroups();

    @GetMapping("/groups/available")
    List<ToolGroupModel> getAvailableToolGroups();
}