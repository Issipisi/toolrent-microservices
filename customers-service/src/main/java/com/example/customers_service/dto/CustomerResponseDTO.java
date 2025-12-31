// En /dto/CustomerResponseDTO.java
package com.example.customers_service.dto;

import com.example.customers_service.entity.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponseDTO {
    private Long id;
    private String name;
    private String rut;
    private String phone;
    private String email;
    private CustomerStatus status;
}