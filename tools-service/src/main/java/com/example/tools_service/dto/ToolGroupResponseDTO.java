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
    private Long availableCount;
    private Long totalUnits; // NUEVO: Total de unidades en el grupo

    // Constructor para compatibilidad
    public ToolGroupResponseDTO(Long id, String name, String category, Double replacementValue,
                                Long tariffId, Double dailyRentalRate, Double dailyFineRate,
                                Long availableCount) {
        this(id, name, category, replacementValue, tariffId, dailyRentalRate,
                dailyFineRate, availableCount, null);
    }

}