package com.example.loans_service.client;

import com.example.loans_service.model.KardexRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "kardex-service", path = "/api/kardex")
public interface KardexClient {

    @PostMapping("/movements")
    void registerMovement(@RequestBody KardexRequest request);
}