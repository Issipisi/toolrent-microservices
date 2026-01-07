package com.example.customers_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "loans-service", path = "/api/loans")
public interface LoanClient {

    @GetMapping("/{customerId}/active-count")
    Integer getActiveLoansCount(@PathVariable("customerId") Long customerId);

    @GetMapping("/{customerId}/overdue-count")
    Long getOverdueLoansCount(@PathVariable("customerId") Long customerId);

    @GetMapping("/{customerId}/unpaid-fines-sum")
    Double getUnpaidFinesSum(@PathVariable("customerId") Long customerId);

    @GetMapping("/{customerId}/unpaid-damage-sum")
    Double getUnpaidDamageSum(@PathVariable("customerId") Long customerId);
}