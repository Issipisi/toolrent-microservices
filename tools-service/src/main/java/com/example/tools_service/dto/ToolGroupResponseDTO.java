package com.example.tools_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolGroupResponseDTO {
    private Long id;
    private String name;
    private String category;
    private Double replacementValue;
    private Long tariffId;
    private Double dailyRentalRate;
    private Double dailyFineRate;
    private Long availableUnits;
}