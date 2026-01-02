package com.example.reports_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDelayReportDTO {
    private Long customerId;
    private String customerName;
    private String customerRut;
    private String customerEmail;
    private Integer overdueLoansCount;
    private Long maxDaysOverdue;
    private Double totalFines;
    private String status; // "RESTRINGIDO" o "ACTIVO"
    private LocalDateTime lastOverdueDate;
}