package com.example.kardex_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "kardex_movements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KardexMovementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tool_unit_id", nullable = false)
    private Long toolUnitId;

    @Column(name = "tool_group_id", nullable = false)
    private Long toolGroupId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "user_id", nullable = false)
    private Long userId = 0L; // 0 = SISTEMA por defecto

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType movementType;

    @Column(nullable = false)
    private LocalDateTime movementDate = LocalDateTime.now();

    @Column(length = 500)
    private String details;

    // Campos denormalizados para consultas
    private String toolGroupName;
    private String customerName;
    private String userName = "SISTEMA"; // Por defecto

    // Auditoría
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (movementDate == null) {
            movementDate = LocalDateTime.now();
        }
    }
}