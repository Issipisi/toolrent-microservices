package com.example.tools_service.repository;

import com.example.tools_service.entity.ToolGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToolGroupRepository extends JpaRepository<ToolGroupEntity, Long> {
    Optional<ToolGroupEntity> findByName(String name);
    boolean existsByName(String name);
}