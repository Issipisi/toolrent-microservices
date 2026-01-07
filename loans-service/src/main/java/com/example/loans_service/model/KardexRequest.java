package com.example.loans_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KardexRequest {
    private MovementType movementType;
    private Long toolUnitId;
    private Long toolGroupId;
    private Long customerId;
    private String details;
    private String userId;
}

