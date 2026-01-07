package com.example.loans_service.controller;

import com.example.loans_service.dto.LoanActiveDTO;
import com.example.loans_service.dto.LoanRequestDTO;
import com.example.loans_service.dto.LoanResponseDTO;
import com.example.loans_service.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Slf4j
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanResponseDTO> registerLoan(@RequestBody LoanRequestDTO request,
                                                        @RequestParam(required = false) String userName) {
        LoanResponseDTO loan = loanService.registerLoan(request, userName);
        return ResponseEntity.ok(loan);
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<LoanResponseDTO> returnLoan(
            @PathVariable Long id,
            @RequestParam(required = false) Double damageCharge,
            @RequestParam(required = false, defaultValue = "false") Boolean irreparable,
            @RequestParam(required = false) String userName) {
        LoanResponseDTO returnedLoan = loanService.returnLoan(id, damageCharge, irreparable, userName);
        return ResponseEntity.ok(returnedLoan);
    }

    @GetMapping("/active")
    public ResponseEntity<List<LoanActiveDTO>> getActiveLoans() {
        try {
            List<LoanActiveDTO> loans = loanService.getActiveLoans();
            return ResponseEntity.ok(loans != null ? loans : new ArrayList<>());
        } catch (Exception e) {
            log.error("❌ Error en endpoint /active: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ArrayList<>());
        }
    }

    @GetMapping("/returned-with-debts")
    public ResponseEntity<List<LoanActiveDTO>> getReturnedWithDebts() {
        try {
            List<LoanActiveDTO> loans = loanService.getReturnedWithDebts();
            return ResponseEntity.ok(loans != null ? loans : new ArrayList<>());
        } catch (Exception e) {
            log.error("❌ Error en endpoint /returned-with-debts: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ArrayList<>());
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<LoanActiveDTO>> getOverdueLoans() {
        List<LoanActiveDTO> loans = loanService.getOverdueLoans();
        return ResponseEntity.ok(loans);
    }

    @PutMapping("/{id}/pay-debts")
    public ResponseEntity<Void> payDebts(@PathVariable Long id) {
        loanService.payDebts(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/damage")
    public ResponseEntity<Void> applyDamage(
            @PathVariable Long id,
            @RequestParam Double amount,
            @RequestParam(defaultValue = "false") boolean irreparable) {
        loanService.applyDamage(id, amount, irreparable);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all-closed")
    public ResponseEntity<List<LoanActiveDTO>> getAllClosedLoans() {
        List<LoanActiveDTO> loans = loanService.getAllClosedLoans();
        return ResponseEntity.ok(loans);
    }


    //Datos Customer
    @GetMapping("/{customerId}/active-count")
    public ResponseEntity<Integer> getActiveLoansCount(@PathVariable Long customerId) {
        int count = loanService.getActiveLoans().size(); // o usar query directa
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{customerId}/overdue-count")
    public ResponseEntity<Long> getOverdueLoansCount(@PathVariable Long customerId) {
        long count = loanService.getOverdueLoansCount(customerId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{customerId}/unpaid-fines-sum")
    public ResponseEntity<Double> getUnpaidFinesSum(@PathVariable Long customerId) {
        double sum = loanService.getUnpaidFinesSum(customerId);
        return ResponseEntity.ok(sum);
    }

    @GetMapping("/{customerId}/unpaid-damage-sum")
    public ResponseEntity<Double> getUnpaidDamageSum(@PathVariable Long customerId) {
        double sum = loanService.getUnpaidDamageSum(customerId);
        return ResponseEntity.ok(sum);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}