// KardexMovementEntity.java
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

    @Column(name = "customer_id")  // <- REMOVER nullable = false
    private Long customerId;        // <- Puede ser null

    @Column(name = "user_id", nullable = false)
    private Long userId = 0L;  // Valor por defecto para usuario SISTEMA

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType movementType;

    @Column(nullable = false)
    private LocalDateTime movementDate = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String details;

    // Campos denormalizados
    @Column(name = "tool_group_name")
    private String toolGroupName;

    @Column(name = "customer_name")
    private String customerName = "N/A";  // Valor por defecto

    @Column(name = "user_name")
    private String userName = "SISTEMA";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}