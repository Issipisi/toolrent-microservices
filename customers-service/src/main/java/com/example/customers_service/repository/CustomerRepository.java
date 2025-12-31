package com.example.customers_service.repository;

import com.example.customers_service.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    Optional<CustomerEntity> findByRut(String rut);
    Optional<CustomerEntity> findByEmail(String email);
    boolean existsByRut(String rut);
    boolean existsByEmail(String email);
}