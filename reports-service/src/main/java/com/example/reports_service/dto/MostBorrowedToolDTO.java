package com.example.reports_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MostBorrowedToolDTO {
    private Long toolGroupId;
    private String toolName;
    private String category;
    private Long loanCount;  // Cantidad de préstamos
    private Long availableStock; // Stock disponible
    private Double replacementValue;

}