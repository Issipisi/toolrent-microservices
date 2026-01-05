package com.example.customers_service.controller;

import com.example.customers_service.dto.*;
import com.example.customers_service.entity.CustomerStatus;
import com.example.customers_service.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(
            @Valid @RequestBody CustomerRequestDTO request) {
        CustomerResponseDTO created = customerService.createCustomer(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomer(@PathVariable Long id) {
        CustomerResponseDTO customer = customerService.getCustomer(id);
        return ResponseEntity.ok(customer);
    }

    // ENDPOINT ESPECIAL para Loan Service
    @GetMapping("/{id}/validate-loan")
    public ResponseEntity<CustomerValidationDTO> validateForLoan(@PathVariable Long id) {
        CustomerValidationDTO validation = customerService.validateForLoan(id);
        return ResponseEntity.ok(validation);
    }

    // ENDPOINT para contar préstamos activos
    @GetMapping("/{id}/active-count")
    public ResponseEntity<Integer> getActiveLoansCount(@PathVariable Long id) {
        Integer count = customerService.getActiveLoansCount(id);
        return ResponseEntity.ok(count);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        List<CustomerResponseDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CustomerResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam CustomerStatus status) {
        CustomerResponseDTO updated = customerService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CustomerResponseDTO>> getActiveCustomers() {
        List<CustomerResponseDTO> customers = customerService.getActiveCustomers();
        return ResponseEntity.ok(customers);
    }
}