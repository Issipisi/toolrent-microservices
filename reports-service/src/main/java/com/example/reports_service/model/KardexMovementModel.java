package com.example.reports_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KardexMovementModel {
    private Long id;
    private Long toolUnitId;
    private String toolGroupName;
    private Long customerId;
    private String customerName;
    private String movementType;
    private LocalDateTime movementDate;
    private String details;
}