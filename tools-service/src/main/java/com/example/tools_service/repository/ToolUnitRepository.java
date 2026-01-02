package com.example.tools_service.repository;

import com.example.tools_service.entity.ToolStatus;
import com.example.tools_service.entity.ToolUnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToolUnitRepository extends JpaRepository<ToolUnitEntity, Long> {

    // Para Loan Service: encontrar primera unidad disponible de un grupo
    Optional<ToolUnitEntity> findFirstByToolGroupIdAndStatus(Long toolGroupId, ToolStatus status);

    // Contar unidades disponibles de un grupo
    long countByToolGroupIdAndStatus(Long toolGroupId, ToolStatus status);

    // Buscar unidad con su grupo (para respuestas completas)
    @Query("SELECT u FROM ToolUnitEntity u JOIN FETCH u.toolGroup WHERE u.id = :id")
    Optional<ToolUnitEntity> findByIdWithGroup(@Param("id") Long id);
}