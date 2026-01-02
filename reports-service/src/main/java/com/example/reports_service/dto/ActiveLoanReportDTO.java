package com.example.reports_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveLoanReportDTO {
    private Long loanId;
    private String customerName;
    private String toolName;
    private LocalDateTime loanDate;
    private LocalDateTime dueDate;
    private Long daysOverdue; // 0 si no está vencido
    private Double fineAmount;
    private String status;
}