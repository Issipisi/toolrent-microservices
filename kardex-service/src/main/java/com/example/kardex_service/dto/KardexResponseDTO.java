package com.example.kardex_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KardexResponseDTO {
    private Long id;
    private Long toolUnitId;
    private Long toolGroupId;
    private String toolGroupName;
    private Long customerId;
    private String customerName;
    private String userId;
    private String userName;
    private String movementType;
    private LocalDateTime movementDate;
    private String details;
}