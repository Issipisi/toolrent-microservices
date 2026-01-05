package com.example.kardex_service.client;

import com.example.kardex_service.model.CustomerModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// CustomerClient.java
@FeignClient(
        name = "customers-service",
        contextId = "kardexCustomerClient",  // ← CONTEXT ID ÚNICO
        path = "/api/customers"
)
public interface CustomerClient {
    @GetMapping("/{id}")
    CustomerModel getCustomer(@PathVariable Long id);
}