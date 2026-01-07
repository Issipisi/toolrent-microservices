package com.example.customers_service.service;

import com.example.customers_service.client.LoanClient;
import com.example.customers_service.dto.*;
import com.example.customers_service.entity.CustomerEntity;
import com.example.customers_service.entity.CustomerStatus;
import com.example.customers_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final LoanClient loanClient;

    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO request) {
        // Validar RUT único
        if (customerRepository.existsByRut(request.getRut())) {
            throw new RuntimeException("El RUT ya está registrado");
        }

        // Validar email único
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        CustomerEntity customer = new CustomerEntity();
        customer.setName(request.getName());
        customer.setRut(request.getRut());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setStatus(CustomerStatus.ACTIVE);

        CustomerEntity saved = customerRepository.save(customer);
        return mapToDTO(saved);
    }

    public CustomerResponseDTO getCustomer(Long id) {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return mapToDTO(customer);
    }

    // MÉTODO ESPECIAL para Loan Service
    public CustomerValidationDTO validateForLoan(Long customerId) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Obtener datos reales desde Loan Service
        int activeCount = loanClient.getActiveLoansCount(customerId);
        long overdueCount = loanClient.getOverdueLoansCount(customerId);
        double unpaidFines = loanClient.getUnpaidFinesSum(customerId);
        double unpaidDamage = loanClient.getUnpaidDamageSum(customerId);

        CustomerValidationDTO dto = new CustomerValidationDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setStatus(customer.getStatus().toString());
        dto.setHasOverdueLoans(overdueCount > 0);
        dto.setHasUnpaidFines(unpaidFines > 0);
        dto.setHasUnpaidDamage(unpaidDamage > 0);
        dto.setActiveLoansCount(activeCount);

        return dto;
    }

    public Integer getActiveLoansCount(Long customerId) {
        // Este método será implementado cuando Loan Service exista
        // Por ahora retorna 0
        return 0;
    }

    public List<CustomerResponseDTO> getActiveCustomers() {
        return customerRepository.findByStatus(CustomerStatus.ACTIVE).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerResponseDTO updateStatus(Long id, CustomerStatus status) {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        customer.setStatus(status);
        CustomerEntity updated = customerRepository.save(customer);
        return mapToDTO(updated);
    }

    private CustomerResponseDTO mapToDTO(CustomerEntity entity) {
        return new CustomerResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getRut(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getStatus()
        );
    }

    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}