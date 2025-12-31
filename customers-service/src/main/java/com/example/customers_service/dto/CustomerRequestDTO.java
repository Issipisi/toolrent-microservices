// En /dto/CustomerRequestDTO.java
package com.example.customers_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequestDTO {
    @NotBlank(message = "Nombre es requerido")
    private String name;

    @NotBlank(message = "RUT es requerido")
    @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "RUT inválido")
    private String rut;

    @NotBlank(message = "Teléfono es requerido")
    private String phone;

    @NotBlank(message = "Email es requerido")
    @Email(message = "Email inválido")
    private String email;
}