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

    // NO más @ManyToOne con CustomerEntity
    // En microservicios guardamos solo el ID
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    // NO más @ManyToOne con ToolUnitEntity
    @Column(name = "tool_unit_id", nullable = false)
    private Long toolUnitId;

    // NO más relaciones JPA, solo IDs
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

    // Nuevo: status para facilitar consultas
    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.ACTIVE;

    public enum LoanStatus {
        ACTIVE, RETURNED, OVERDUE, DAMAGED
    }
}