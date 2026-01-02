package com.example.tools_service.model;

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
    private Long tariffId; // ← NUEVO: para que Loan Service pueda buscar tarifa

    // Datos del grupo para cálculos en Loan Service
    private String toolGroupName;
    private Double dailyRentalRate; // ← Traído de Tariff Service
    private Double dailyFineRate;   // ← Traído de Tariff Service
    private Double replacementValue;
}