package com.example.tools_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TariffModel {
    private Long id;
    private Double dailyRentalRate;
    private Double dailyFineRate;
}