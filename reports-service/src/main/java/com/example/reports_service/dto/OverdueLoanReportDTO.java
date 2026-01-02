package com.example.reports_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverdueLoanReportDTO {
    private Long loanId;
    private String customerName;
    private String customerRut;
    private String customerEmail;
    private String toolName;
    private LocalDateTime loanDate;
    private LocalDateTime dueDate;
    private Long daysOverdue;
    private Double fineAmount;
    private String customerPhone; // Para contacto
}