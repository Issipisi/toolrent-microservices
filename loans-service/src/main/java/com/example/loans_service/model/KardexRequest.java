package com.example.loans_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KardexRequest {
    private String movementType; // "LOAN", "RETURN", "RETIRE", etc.
    private Long toolUnitId;
    private Long customerId;
    private String details;
}