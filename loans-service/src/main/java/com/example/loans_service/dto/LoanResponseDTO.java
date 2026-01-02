package com.example.loans_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponseDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long toolUnitId;
    private String toolName;
    private LocalDateTime loanDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private Double totalCost;
    private Double fineAmount;
    private Double damageCharge;
    private String status;
}