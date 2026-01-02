package com.example.tariffs_service.repository;

import com.example.tariffs_service.entity.TariffEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TariffRepository extends JpaRepository<TariffEntity, Long> {
}