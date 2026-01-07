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

    @GetMapping("/tool-ranking")
    public ResponseEntity<List<ToolRankingDTO>> getToolRanking() {
        return ResponseEntity.ok(reportService.getToolRankingByLoans());
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Reports Service is running - Solo 3 reportes!");
    }
}