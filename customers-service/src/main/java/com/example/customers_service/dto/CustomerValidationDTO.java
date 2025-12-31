// En /dto/CustomerValidationDTO.java - ESPECIAL para Loan Service
package com.example.customers_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerValidationDTO {
    private Long id;
    private String name;
    private String status; // "ACTIVE" o "RESTRICTED"

    // Campos que Loan Service necesita para validar
    private boolean hasOverdueLoans;
    private boolean hasUnpaidFines;
    private boolean hasUnpaidDamage;
    private int activeLoansCount;
}