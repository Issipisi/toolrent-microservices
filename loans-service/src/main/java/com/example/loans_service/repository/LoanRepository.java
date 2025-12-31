package com.example.loans_service.repository;

import com.example.loans_service.entity.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity, Long> {

    List<LoanEntity> findByReturnDateIsNull();
    List<LoanEntity> findByCustomerIdAndReturnDateIsNull(Long customerId);
    boolean existsByCustomerIdAndReturnDateIsNullAndDueDateBefore(Long customerId, LocalDateTime date);
    boolean existsByCustomerIdAndToolGroupIdAndReturnDateIsNull(Long customerId, Long toolGroupId);
    long countByCustomerIdAndReturnDateIsNull(Long customerId);
    List<LoanEntity> findByReturnDateIsNotNullAndFineAmountGreaterThanOrDamageChargeGreaterThan(
            Double fineMin, Double damageMin);
}
