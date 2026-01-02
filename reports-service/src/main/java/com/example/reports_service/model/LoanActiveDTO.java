package com.example.reports_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanActiveDTO {
    private Long id;
    private String customerName;
    private String toolName;
    private LocalDateTime loanDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private Double fineAmount;
    private Double damageCharge;
    private String status;
}