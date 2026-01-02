package com.example.reports_service.controller;

import com.example.reports_service.dto.*;
import com.example.reports_service.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // RF6.1
    @GetMapping("/active-loans")
    public ResponseEntity<List<ActiveLoanReportDTO>> getActiveLoans() {
        return ResponseEntity.ok(reportService.getActiveLoansReport());
    }

    // RF6.2
    @GetMapping("/customers-with-delays")
    public ResponseEntity<List<CustomerDelayReportDTO>> getCustomersWithDelays() {
        return ResponseEntity.ok(reportService.getCustomersWithDelaysReport());
    }

    // RF6.3
    @GetMapping("/most-borrowed-tools")
    public ResponseEntity<List<MostBorrowedToolDTO>> getMostBorrowedTools(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Si no se proporcionan fechas, usar últimos 30 días
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        return ResponseEntity.ok(reportService.getMostBorrowedToolsReport(startDate, endDate));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Reports Service is running - Solo 3 reportes!");
    }
}