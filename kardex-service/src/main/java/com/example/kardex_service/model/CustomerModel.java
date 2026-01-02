package com.example.kardex_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerModel {
    private Long id;
    private String name;
    private String rut;
    private String status;
    private boolean hasOverdueLoans;
    private boolean hasUnpaidFines;
    private boolean hasUnpaidDamage;
    private int activeLoansCount;
}