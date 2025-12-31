package com.example.loans_service.service;

import com.example.loans_service.client.*;
import com.example.loans_service.dto.LoanResponseDTO;
import com.example.loans_service.entity.LoanEntity;
import com.example.loans_service.entity.LoanEntity.LoanStatus;
import com.example.loans_service.model.*;
import com.example.loans_service.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final CustomerClient customerClient;
    private final ToolClient toolClient;
    private final KardexClient kardexClient;

    @Transactional
    public LoanEntity registerLoan(Long toolGroupId, Long customerId, LocalDateTime dueDate) {
        log.info("Registrando préstamo - Cliente: {}, Herramienta: {}", customerId, toolGroupId);

        // 1. Validar fecha
        if (dueDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de devolución no puede ser anterior a la actual");
        }

        // 2. Validar cliente usando Feign
        CustomerModel customer = customerClient.validateForLoan(customerId);

        if (customer == null) {
            throw new RuntimeException("Cliente no encontrado");
        }

        if ("RESTRICTED".equals(customer.getStatus())) {
            throw new RuntimeException("Cliente restringido");
        }

        if (customer.isHasOverdueLoans()) {
            throw new RuntimeException("El cliente tiene préstamos vencidos sin devolver");
        }

        if (customer.isHasUnpaidFines() || customer.isHasUnpaidDamage()) {
            throw new RuntimeException("El cliente tiene deudas pendientes");
        }

        // 3. Validar límite de préstamos
        if (customer.getActiveLoansCount() >= 5) {
            throw new RuntimeException("Máximo 5 préstamos activos permitidos");
        }

        // 4. Verificar si ya tiene la misma herramienta
        boolean alreadyHasSameTool = loanRepository
                .existsByCustomerIdAndToolGroupIdAndReturnDateIsNull(customerId, toolGroupId);
        if (alreadyHasSameTool) {
            throw new RuntimeException("Ya tiene esta herramienta en préstamo");
        }

        // 5. Obtener herramienta disponible
        ToolUnitModel toolUnit = toolClient.getAvailableUnit(toolGroupId);
        if (toolUnit == null) {
            throw new RuntimeException("No hay unidades disponibles");
        }

        if (!"AVAILABLE".equals(toolUnit.getStatus())) {
            throw new RuntimeException("La herramienta no está disponible");
        }

        // 6. Calcular costo (mínimo 1 día)
        long days = Math.max(1, ChronoUnit.DAYS.between(LocalDateTime.now(), dueDate));
        Double totalCost = toolUnit.getDailyRentalRate() * days;

        // 7. Crear préstamo
        LoanEntity loan = new LoanEntity();
        loan.setCustomerId(customerId);
        loan.setToolUnitId(toolUnit.getId());
        loan.setToolGroupId(toolGroupId);
        loan.setDueDate(dueDate);
        loan.setTotalCost(totalCost);
        loan.setStatus(LoanStatus.ACTIVE);

        LoanEntity savedLoan = loanRepository.save(loan);

        // 8. Actualizar estado de la herramienta a LOANED
        try {
            toolClient.updateStatus(toolUnit.getId(), "LOANED");
        } catch (Exception e) {
            log.error("Error actualizando estado de herramienta", e);
            throw new RuntimeException("Error al actualizar estado de herramienta");
        }

        // 9. Registrar movimiento en Kardex
        try {
            kardexClient.registerMovement(new KardexRequest(
                    "LOAN",
                    toolUnit.getId(),
                    customerId,
                    "Préstamo ID: " + savedLoan.getId(),
                    LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.warn("Error registrando en Kardex, pero préstamo creado", e);
        }

        return savedLoan;
    }

    // Más métodos...
}