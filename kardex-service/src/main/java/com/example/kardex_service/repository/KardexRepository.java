package com.example.kardex_service.repository;

import com.example.kardex_service.entity.KardexMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface KardexRepository extends JpaRepository<KardexMovementEntity, Long> {

    List<KardexMovementEntity> findByToolUnitId(Long toolUnitId);

    List<KardexMovementEntity> findByCustomerId(Long customerId);

    @Query("SELECT k FROM KardexMovementEntity k WHERE k.movementDate BETWEEN :from AND :to ORDER BY k.movementDate DESC")
    List<KardexMovementEntity> findByMovementDateBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // Método CORREGIDO usando @Query con string
    @Query("SELECT k FROM KardexMovementEntity k WHERE UPPER(k.movementType) = UPPER(:movementType) ORDER BY k.movementDate DESC")
    List<KardexMovementEntity> findByMovementType(@Param("movementType") String movementType);
}