package com.example.loans_service.service;

import com.example.loans_service.client.*;
import com.example.loans_service.dto.LoanActiveDTO;
import com.example.loans_service.dto.LoanRequestDTO;
import com.example.loans_service.dto.LoanResponseDTO;
import com.example.loans_service.entity.LoanEntity;
import com.example.loans_service.entity.LoanStatus;
import com.example.loans_service.model.*;
import com.example.loans_service.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
    public LoanResponseDTO registerLoan(LoanRequestDTO request, String userName) {
        log.info("Registrando préstamo - Cliente: {}, Herramienta: {}",
                request.getCustomerId(), request.getToolGroupId());

        // 1. Parsear fecha
        LocalDateTime dueDate = LocalDateTime.parse(request.getDueDate());

        // 2. Validar fecha
        if (dueDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("La fecha de devolución no puede ser anterior a la actual");
        }

        // 3. Validar cliente
        CustomerModel customer = customerClient.validateForLoan(request.getCustomerId());
        validateCustomerForLoan(customer);

        // 4. Validar límites de préstamo
        validateLoanLimits(request.getCustomerId(), request.getToolGroupId());

        // 5. Obtener herramienta disponible
        ToolUnitModel toolUnit = toolClient.getAvailableUnit(request.getToolGroupId());

        // 6. Calcular costo
        long days = Math.max(1, ChronoUnit.DAYS.between(LocalDateTime.now(), dueDate));
        Double totalCost = toolUnit.getDailyRentalRate() * days;

        // 7. Crear préstamo
        LoanEntity loan = new LoanEntity();
        loan.setCustomerId(request.getCustomerId());
        loan.setToolUnitId(toolUnit.getId());
        loan.setToolGroupId(request.getToolGroupId());
        loan.setDueDate(dueDate);
        loan.setTotalCost(totalCost);
        loan.setStatus(LoanStatus.ACTIVE);

        LoanEntity savedLoan = loanRepository.save(loan);

        // 8. Actualizar estado de herramienta
        try {
            toolClient.updateStatus(toolUnit.getId(), "LOANED", userName);
        } catch (Exception e) {
            log.error("Error actualizando estado de herramienta", e);
            throw new RuntimeException("Error al actualizar estado de herramienta");
        }

        // 9. Registrar en Kardex
        try {
            KardexRequest requestKardex = new KardexRequest();
            requestKardex.setMovementType(MovementType.LOAN);
            requestKardex.setToolUnitId(toolUnit.getId());
            requestKardex.setToolGroupId(request.getToolGroupId());
            requestKardex.setCustomerId(request.getCustomerId());
            requestKardex.setDetails("Préstamo a cliente: " + customer.getName());
            requestKardex.setUserId(userName != null ? userName : "Sistema");

            kardexClient.registerMovement(requestKardex);
        } catch (Exception e) {
            log.warn("Error registrando en Kardex, pero préstamo creado", e);
        }

        return mapToResponseDTO(savedLoan, customer.getName(), toolUnit.getToolGroupName());
    }

    @Transactional
    public LoanResponseDTO returnLoan(Long loanId, Double damageCharge, boolean irreparable, String userName) {
        log.info("Devolviendo préstamo: {} - Daño: {} - Irreparable: {}",
                loanId, damageCharge, irreparable);

        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        // 1. Actualizar fechas
        loan.setReturnDate(LocalDateTime.now());

        // Obtener nombre del cliente para Kardex
        CustomerModel customer = customerClient.getCustomer(loan.getCustomerId());
        String customerName = customer != null ? customer.getName() : "Desconocido";

        // 2. Calcular multa por atraso
        if (loan.getReturnDate().isAfter(loan.getDueDate())) {
            long lateDays = ChronoUnit.DAYS.between(loan.getDueDate(), loan.getReturnDate());
            ToolUnitModel toolUnit = toolClient.getToolUnit(loan.getToolUnitId());
            loan.setFineAmount(lateDays * toolUnit.getDailyFineRate());
        }

        // 3. Manejar daños
        if (irreparable) {
            ToolUnitModel toolUnit = toolClient.getToolUnit(loan.getToolUnitId());
            loan.setDamageCharge(toolUnit.getReplacementValue());
            loan.setStatus(LoanStatus.RETURNED);

            // Cambiar estado de herramienta a RETIRED
            toolClient.updateStatus(loan.getToolUnitId(), "RETIRED", userName);

            // No registrar aquí, ToolUnitService ya lo hará
            log.info("Retiro por daño irreparable, ToolUnitService registrará en Kardex");
        } else if (damageCharge != null && damageCharge > 0) {
            loan.setDamageCharge(damageCharge);
            loan.setStatus(LoanStatus.RETURNED);

            // Cambiar estado a IN_REPAIR
            toolClient.updateStatus(loan.getToolUnitId(), "IN_REPAIR", userName);

            // No registrar aquí, ToolUnitService ya lo hará
            log.info("Retiro por daño irreparable, ToolUnitService registrará en Kardex");
        } else {
            loan.setStatus(LoanStatus.RETURNED);

            // Liberar herramienta
            toolClient.updateStatus(loan.getToolUnitId(), "AVAILABLE", userName);

            // Registrar en Kardex como RETURN
            registerKardexMovement("RETURN", loan,
                    "Herramienta Disponible - Devolución normal", userName, customerName);
        }

        LoanEntity updatedLoan = loanRepository.save(loan);

        // Obtener datos para respuesta
        ToolUnitModel toolUnit = toolClient.getToolUnit(loan.getToolUnitId());

        return mapToResponseDTO(updatedLoan, customer.getName(), toolUnit.getToolGroupName());
    }

    public List<LoanActiveDTO> getActiveLoans() {
        try {
            List<LoanEntity> loans = loanRepository.findByReturnDateIsNull();

            if (loans.isEmpty()) {
                log.info(" No hay préstamos activos");
                return new ArrayList<>();
            }

            return loans.stream()
                    .map(loan -> {
                        try {
                            // Intentar obtener información del cliente
                            CustomerModel customer = null;
                            try {
                                customer = customerClient.getCustomer(loan.getCustomerId());
                            } catch (Exception e) {
                                //log.warn(" No se pudo obtener cliente {}: {}", loan.getCustomerId(), e.getMessage());
                            }

                            // Intentar obtener información de la herramienta
                            ToolUnitModel toolUnit = null;
                            try {
                                toolUnit = toolClient.getToolUnit(loan.getToolUnitId());
                            } catch (Exception e) {
                                //log.warn(" No se pudo obtener herramienta {}: {}", loan.getToolUnitId(), e.getMessage());
                            }

                            // Construir DTO con información disponible
                            String customerName = customer != null ? customer.getName() : "Cliente #" + loan.getCustomerId();
                            String toolName = toolUnit != null ? toolUnit.getToolGroupName() : "Herramienta #" + loan.getToolUnitId();

                            return new LoanActiveDTO(
                                    loan.getId(),
                                    customerName,
                                    toolName,
                                    loan.getLoanDate(),
                                    loan.getDueDate(),
                                    loan.getReturnDate(),
                                    loan.getFineAmount() != null ? loan.getFineAmount() : 0.0,
                                    loan.getDamageCharge() != null ? loan.getDamageCharge() : 0.0,
                                    loan.getStatus().toString()
                            );
                        } catch (Exception e) {
                            log.error(" Error mapeando préstamo activo ID {}: {}", loan.getId(), e.getMessage());
                            return new LoanActiveDTO(
                                    loan.getId(),
                                    "Error al cargar cliente",
                                    "Error al cargar herramienta",
                                    loan.getLoanDate(),
                                    loan.getDueDate(),
                                    loan.getReturnDate(),
                                    0.0,
                                    0.0,
                                    "ERROR"
                            );
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error(" Error catastrófico obteniendo préstamos activos: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<LoanActiveDTO> getReturnedWithDebts() {
        try {
            log.info(" Obteniendo préstamos devueltos con deudas...");

            // Obtener préstamos con deudas usando múltiples métodos
            List<LoanEntity> loans = new ArrayList<>();

            try {
                loans = loanRepository.findReturnedWithDebts();
                log.info(" Consulta JPQL estándar: {} resultados", loans.size());
            } catch (Exception e1) {
                log.warn("️ Falló consulta estándar, intentando alternativa: {}", e1.getMessage());

                try {
                    loans = loanRepository.findReturnedWithDebtsCoalesced();
                    log.info(" Consulta COALESCE: {} resultados", loans.size());
                } catch (Exception e2) {
                    log.warn(" Falló COALESCE, usando método manual: {}", e2.getMessage());

                    // Método manual como último recurso
                    loans = loanRepository.findByReturnDateIsNotNull().stream()
                            .filter(loan -> {
                                double fine = loan.getFineAmount() != null ? loan.getFineAmount() : 0.0;
                                double damage = loan.getDamageCharge() != null ? loan.getDamageCharge() : 0.0;
                                return fine > 0 || damage > 0;
                            })
                            .collect(Collectors.toList());
                    log.info(" Método manual: {} resultados", loans.size());
                }
            }

            if (loans.isEmpty()) {
                log.info(" No hay préstamos devueltos con deudas");
                return new ArrayList<>();
            }

            return loans.stream()
                    .map(loan -> {
                        try {
                            CustomerModel customer = null;
                            try {
                                customer = customerClient.getCustomer(loan.getCustomerId());
                            } catch (Exception e) {
                                log.warn(" No se pudo obtener cliente {}: {}", loan.getCustomerId(), e.getMessage());
                            }

                            ToolUnitModel toolUnit = null;
                            try {
                                toolUnit = toolClient.getToolUnit(loan.getToolUnitId());
                            } catch (Exception e) {
                                log.warn(" No se pudo obtener herramienta {}: {}", loan.getToolUnitId(), e.getMessage());
                            }

                            String customerName = customer != null ? customer.getName() : "Cliente #" + loan.getCustomerId();
                            String toolName = toolUnit != null ? toolUnit.getToolGroupName() : "Herramienta #" + loan.getToolUnitId();

                            return new LoanActiveDTO(
                                    loan.getId(),
                                    customerName,
                                    toolName,
                                    loan.getLoanDate(),
                                    loan.getDueDate(),
                                    loan.getReturnDate(),
                                    loan.getFineAmount() != null ? loan.getFineAmount() : 0.0,
                                    loan.getDamageCharge() != null ? loan.getDamageCharge() : 0.0,
                                    "RETURNED_WITH_DEBTS"
                            );
                        } catch (Exception e) {
                            log.error(" Error mapeando préstamo con deudas ID {}: {}", loan.getId(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error(" Error catastrófico obteniendo préstamos con deudas: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<LoanActiveDTO> getOverdueLoans() {
        List<LoanEntity> loans = loanRepository.findOverdueLoans(LocalDateTime.now());

        return loans.stream().map(loan -> {
            CustomerModel customer = getCustomerInfo(loan.getCustomerId());
            ToolUnitModel toolUnit = getToolUnitInfo(loan.getToolUnitId());

            return new LoanActiveDTO(
                    loan.getId(),
                    customer != null ? customer.getName() : "Desconocido",
                    toolUnit != null ? toolUnit.getToolGroupName() : "Desconocido",
                    loan.getLoanDate(),
                    loan.getDueDate(),
                    loan.getReturnDate(),
                    loan.getFineAmount(),
                    loan.getDamageCharge(),
                    "OVERDUE"
            );
        }).collect(Collectors.toList());
    }

    public List<LoanActiveDTO> getAllClosedLoans() {
        List<LoanEntity> closedLoans = loanRepository.findByReturnDateIsNotNull();
        return closedLoans.stream().map(loan -> {
            CustomerModel customer = getCustomerInfo(loan.getCustomerId());
            ToolUnitModel tool = getToolUnitInfo(loan.getToolUnitId());
            return new LoanActiveDTO(
                    loan.getId(),
                    customer != null ? customer.getName() : "Desconocido",
                    tool != null ? tool.getToolGroupName() : "Desconocido",
                    loan.getLoanDate(),
                    loan.getDueDate(),
                    loan.getReturnDate(),
                    loan.getFineAmount() != null ? loan.getFineAmount() : 0.0,
                    loan.getDamageCharge() != null ? loan.getDamageCharge() : 0.0,
                    "RETURNED"
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public void payDebts(Long loanId) {
        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if (loan.getReturnDate() == null) {
            throw new RuntimeException("Solo se pueden pagar deudas de préstamos devueltos");
        }

        loan.setFineAmount(0.0);
        loan.setDamageCharge(0.0);
        loanRepository.save(loan);
    }

    @Transactional
    public void applyDamage(Long loanId, Double amount, boolean irreparable) {
        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if (loan.getReturnDate() == null) {
            throw new RuntimeException("Solo se puede aplicar daño a préstamos devueltos");
        }

        if (irreparable) {
            ToolUnitModel toolUnit = toolClient.getToolUnit(loan.getToolUnitId());
            loan.setDamageCharge(toolUnit.getReplacementValue());
            loan.setStatus(LoanStatus.RETURNED);
        } else {
            loan.setDamageCharge(amount);
            loan.setStatus(LoanStatus.RETURNED);
        }

        loanRepository.save(loan);
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN
    private void validateCustomerForLoan(CustomerModel customer) {
        if (customer == null) {
            throw new RuntimeException("Cliente no encontrado");
        }

        if ("RESTRICTED".equals(customer.getStatus())) {
            throw new RuntimeException("Cliente restringido");
        }

        if (customer.isHasOverdueLoans()) {
            throw new RuntimeException("El cliente tiene préstamos vencidos sin devolver");
        }

        if (customer.isHasUnpaidFines()) {
            throw new RuntimeException("El cliente tiene multas impagas");
        }

        if (customer.isHasUnpaidDamage()) {
            throw new RuntimeException("El cliente tiene cargos por daño sin pagar");
        }
    }

    public long getOverdueLoansCount(Long customerId) {
        return loanRepository.countByCustomerIdAndReturnDateIsNullAndDueDateBefore(customerId, LocalDateTime.now());
    }

    public double getUnpaidFinesSum(Long customerId) {
        return loanRepository.sumUnpaidFinesByCustomer(customerId);
    }

    public double getUnpaidDamageSum(Long customerId) {
        return loanRepository.sumUnpaidDamageByCustomer(customerId);
    }

    private void validateLoanLimits(Long customerId, Long toolGroupId) {
        // Máximo 5 préstamos activos
        long activeLoans = loanRepository.countByCustomerIdAndReturnDateIsNull(customerId);
        if (activeLoans >= 5) {
            throw new RuntimeException("El cliente ya tiene 5 préstamos activos (máximo permitido)");
        }

        // No puede pedir la MISMA herramienta que ya tiene
        boolean alreadyHasSameTool = loanRepository.existsByCustomerIdAndToolGroupIdAndReturnDateIsNull(
                customerId, toolGroupId);
        if (alreadyHasSameTool) {
            throw new RuntimeException("El cliente ya tiene una unidad de esta herramienta en préstamo");
        }
    }


    private void registerKardexMovement(String movementType,
                                        LoanEntity loan,
                                        String details,
                                        String userName,
                                        String customerName) {
        try {
            KardexRequest request = new KardexRequest();
            request.setMovementType(MovementType.valueOf(movementType)); // LOAN, RETURN, REPAIR, RETIRE
            request.setToolUnitId(loan.getToolUnitId());
            request.setToolGroupId(loan.getToolGroupId());
            request.setCustomerId(loan.getCustomerId());
            request.setDetails(details);
            request.setUserId(userName != null ? userName : "Sistema");

            kardexClient.registerMovement(request);
        } catch (Exception e) {
            log.warn("Error registrando movimiento {} en Kardex: {}", movementType, e.getMessage());
        }
    }



    private LoanResponseDTO mapToResponseDTO(LoanEntity loan, String customerName, String toolName) {
        return new LoanResponseDTO(
                loan.getId(),
                loan.getCustomerId(),
                customerName,
                loan.getToolUnitId(),
                toolName,
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                loan.getTotalCost(),
                loan.getFineAmount(),
                loan.getDamageCharge(),
                loan.getStatus().toString()
        );
    }

    private CustomerModel getCustomerInfo(Long customerId) {
        try {
            return customerClient.getCustomer(customerId);
        } catch (Exception e) {
            log.warn("Error obteniendo cliente {}", customerId, e);
            return null;
        }
    }

    private ToolUnitModel getToolUnitInfo(Long toolUnitId) {
        try {
            return toolClient.getToolUnit(toolUnitId);
        } catch (Exception e) {
            log.warn("Error obteniendo herramienta {}", toolUnitId, e);
            return null;
        }
    }
}