package com.example.loans_service.repository;

import com.example.loans_service.entity.LoanEntity;
import com.example.loans_service.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity, Long> {

    // Contar préstamos activos de un cliente
    long countByCustomerIdAndReturnDateIsNull(Long customerId);

    // Verificar si cliente ya tiene esta herramienta en préstamo
    boolean existsByCustomerIdAndToolGroupIdAndReturnDateIsNull(Long customerId, Long toolGroupId);

    // Préstamos activos (sin devolución)
    List<LoanEntity> findByReturnDateIsNull();

    // Préstamos vencidos
    @Query("SELECT l FROM LoanEntity l WHERE l.returnDate IS NULL AND l.dueDate < :now")
    List<LoanEntity> findOverdueLoans(@Param("now") LocalDateTime now);

    // Préstamos devueltos con deudas
    // Métodos para obtener préstamos con deudas
    @Query("SELECT l FROM LoanEntity l WHERE l.returnDate IS NOT NULL " +
            "AND (l.fineAmount > 0 OR l.damageCharge > 0)")
    List<LoanEntity> findReturnedWithDebts();

    @Query("SELECT l FROM LoanEntity l WHERE l.returnDate IS NOT NULL " +
            "AND (COALESCE(l.fineAmount, 0) > 0 OR COALESCE(l.damageCharge, 0) > 0)")
    List<LoanEntity> findReturnedWithDebtsCoalesced();

    // Método básico como respaldo
    List<LoanEntity> findByReturnDateIsNotNull();

    // Préstamos de un cliente activos
    List<LoanEntity> findByCustomerIdAndReturnDateIsNull(Long customerId);

    // Verificar si cliente tiene préstamos vencidos
    boolean existsByCustomerIdAndReturnDateIsNullAndDueDateBefore(Long customerId, LocalDateTime date);

    // Último préstamo devuelto de una unidad
    Optional<LoanEntity> findTopByToolUnitIdAndReturnDateIsNotNullOrderByReturnDateDesc(Long toolUnitId);

    // Préstamos con multas pendientes
    List<LoanEntity> findByReturnDateIsNotNullAndFineAmountGreaterThan(Double amount);

    // Préstamos con daños pendientes
    List<LoanEntity> findByReturnDateIsNotNullAndDamageChargeGreaterThan(Double amount);
}