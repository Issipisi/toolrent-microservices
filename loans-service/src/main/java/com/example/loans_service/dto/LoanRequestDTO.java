package com.example.loans_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequestDTO {
    private Long toolGroupId;
    private Long customerId;
    private String dueDate; // Formato: "yyyy-MM-dd'T'HH:mm:ss"
}