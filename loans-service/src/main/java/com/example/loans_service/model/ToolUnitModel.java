package com.example.loans_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolUnitModel {
    private Long id;
    private String status; // "AVAILABLE", "LOANED", etc.
    private Long toolGroupId;

    // Datos del grupo (para cálculos)
    private String toolGroupName;
    private Double dailyRentalRate;
    private Double dailyFineRate;
    private Double replacementValue;
}