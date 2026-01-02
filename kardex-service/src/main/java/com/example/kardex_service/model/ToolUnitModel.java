package com.example.kardex_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolUnitModel {
    private Long id;
    private String status;
    private Long toolGroupId;
    private Long tariffId;
    private String toolGroupName;
    private Double dailyRentalRate;
    private Double dailyFineRate;
    private Double replacementValue;
}