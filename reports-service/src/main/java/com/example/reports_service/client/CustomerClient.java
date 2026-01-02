package com.example.reports_service.client;

import com.example.reports_service.model.CustomerModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "customers-service", path = "/api/customers")
public interface CustomerClient {

    @GetMapping("/{id}")
    CustomerModel getCustomer(@PathVariable Long id);

    @GetMapping
    List<CustomerModel> getAllCustomers();
}