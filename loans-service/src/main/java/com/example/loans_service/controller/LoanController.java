package com.example.loans_service.controller;

import com.example.loans_service.dto.LoanActiveDTO;
import com.example.loans_service.dto.LoanRequestDTO;
import com.example.loans_service.dto.LoanResponseDTO;
import com.example.loans_service.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanResponseDTO> registerLoan(@RequestBody LoanRequestDTO request) {
        LoanResponseDTO loan = loanService.registerLoan(request);
        return ResponseEntity.ok(loan);
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<LoanResponseDTO> returnLoan(
            @PathVariable Long id,
            @RequestParam(required = false) Double damageCharge,
            @RequestParam(required = false, defaultValue = "false") Boolean irreparable) {
        LoanResponseDTO returnedLoan = loanService.returnLoan(id, damageCharge, irreparable);
        return ResponseEntity.ok(returnedLoan);
    }

    @GetMapping("/active")
    public ResponseEntity<List<LoanActiveDTO>> getActiveLoans() {
        List<LoanActiveDTO> loans = loanService.getActiveLoans();
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<LoanActiveDTO>> getOverdueLoans() {
        List<LoanActiveDTO> loans = loanService.getOverdueLoans();
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/returned-with-debts")
    public ResponseEntity<List<LoanActiveDTO>> getReturnedWithDebts() {
        List<LoanActiveDTO> loans = loanService.getReturnedWithDebts();
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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}