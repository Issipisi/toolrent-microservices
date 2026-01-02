package com.example.kardex_service.dto;

import com.example.kardex_service.entity.MovementType;
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
    private Long userId; // Opcional, si no se envía se usa SYSTEM
}