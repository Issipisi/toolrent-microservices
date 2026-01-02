package com.example.tools_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolGroupRequestDTO {
    @NotBlank(message = "Nombre es requerido")
    private String name;

    @NotBlank(message = "Categoría es requerida")
    private String category;

    @NotNull(message = "Valor de reposición es requerido")
    @Min(value = 0, message = "Valor de reposición debe ser mayor o igual a 0")
    private Double replacementValue;

    @NotNull(message = "Tarifa diaria de arriendo es requerida")
    @Min(value = 0, message = "Tarifa diaria debe ser mayor o igual a 0")
    private Double dailyRentalRate;

    @NotNull(message = "Tarifa diaria de multa es requerida")
    @Min(value = 0, message = "Tarifa diaria de multa debe ser mayor o igual a 0")
    private Double dailyFineRate;

    @NotNull(message = "Stock inicial es requerido")
    @Min(value = 0, message = "Stock debe ser mayor o igual a 0")
    private Integer initialStock;
}