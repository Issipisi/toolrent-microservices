package com.example.reports_service.client;

import com.example.reports_service.model.LoanActiveDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "loans-service", path = "/api/loans")
public interface LoanClient {

    @GetMapping("/active")
    List<LoanActiveDTO> getActiveLoans();

    @GetMapping("/overdue")
    List<LoanActiveDTO> getOverdueLoans();

    @GetMapping("/returned-with-debts")
    List<LoanActiveDTO> getReturnedWithDebts();

    @GetMapping("/all-closed")
    List<LoanActiveDTO> getAllClosedLoans();

}