package com.example.loans_service.client;

import com.example.loans_service.model.CustomerModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customers-service", path = "/api/customers")
public interface CustomerClient {

    @GetMapping("/{id}")
    CustomerModel getCustomer(@PathVariable Long id);

    @GetMapping("/{id}/validate-loan")
    CustomerModel validateForLoan(@PathVariable Long id);

    @GetMapping("/{id}/active-count")
    Integer getActiveLoansCount(@PathVariable Long id);
}