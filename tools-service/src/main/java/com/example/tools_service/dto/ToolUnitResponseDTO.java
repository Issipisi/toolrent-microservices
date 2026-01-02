package com.example.tools_service.dto;

import com.example.tools_service.entity.ToolStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolUnitResponseDTO {
    private Long id;
    private Long toolGroupId;
    private String toolGroupName;
    private String category;
    private ToolStatus status;
    private Long tariffId;
    private Double dailyRentalRate;
    private Double dailyFineRate;
    private Double replacementValue;
}