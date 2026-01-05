package com.example.reports_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDelayReportDTO {
    private String customerName;
    private String customerRut;
    private String customerEmail;
    private Integer overdueLoansCount;
    private Integer activeLoansCount; // ✅ Nuevo: préstamos activos
    private Long maxDaysOverdue;
    private Double totalDebt;
    private String status; // "ACTIVO" o "RESUELTO"
}