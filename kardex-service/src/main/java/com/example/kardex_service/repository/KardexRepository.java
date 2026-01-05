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

    // Consultas básicas
    List<KardexMovementEntity> findByToolUnitId(Long toolUnitId);

    // CORREGIDO: findByToolGroupId no existe, necesitamos crearlo
    List<KardexMovementEntity> findByToolGroupId(Long toolGroupId);

    List<KardexMovementEntity> findByMovementDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<KardexMovementEntity> findByCustomerId(Long customerId);

    // Por tipo de movimiento (como string, no enum)
    List<KardexMovementEntity> findByMovementType(String movementType);

    // Consultas personalizadas con JPQL
    @Query("SELECT k FROM KardexMovementEntity k WHERE k.toolUnitId = :toolUnitId AND k.movementDate BETWEEN :startDate AND :endDate")
    List<KardexMovementEntity> findByToolUnitIdAndDateRange(
            @Param("toolUnitId") Long toolUnitId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT k FROM KardexMovementEntity k WHERE k.toolGroupId = :toolGroupId AND k.movementDate BETWEEN :startDate AND :endDate")
    List<KardexMovementEntity> findByToolGroupIdAndDateRange(
            @Param("toolGroupId") Long toolGroupId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Último movimiento de una unidad
    KardexMovementEntity findTopByToolUnitIdOrderByMovementDateDesc(Long toolUnitId);

    // Contar movimientos por tipo
    @Query("SELECT COUNT(k) FROM KardexMovementEntity k WHERE k.movementType = :movementType")
    Long countByMovementType(@Param("movementType") String movementType);
}