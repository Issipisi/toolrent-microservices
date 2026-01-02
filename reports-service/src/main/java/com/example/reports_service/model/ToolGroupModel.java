package com.example.reports_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolGroupModel {
    private Long id;
    private String name;
    private String category;
    private Double replacementValue;
    private Long tariffId;
    private Double dailyRentalRate;
    private Double dailyFineRate;
    private Long availableCount;
}