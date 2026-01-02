package com.example.loans_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "tool_unit_id", nullable = false)
    private Long toolUnitId;

    @Column(name = "tool_group_id", nullable = false)
    private Long toolGroupId;

    @Column(nullable = false)
    private LocalDateTime loanDate = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime dueDate;

    private LocalDateTime returnDate;

    private Double totalCost;

    private Double fineAmount = 0.0;

    private Double damageCharge = 0.0;

    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.ACTIVE;
}