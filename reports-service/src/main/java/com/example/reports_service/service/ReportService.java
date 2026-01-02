package com.example.reports_service.service;

import com.example.reports_service.client.*;
import com.example.reports_service.dto.*;
import com.example.reports_service.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final LoanClient loanClient;
    private final CustomerClient customerClient;
    private final ToolClient toolClient;

    // RF6.1: Listar préstamos activos y su estado
    public List<ActiveLoanReportDTO> getActiveLoansReport() {
        try {
            List<LoanActiveDTO> activeLoans = loanClient.getActiveLoans();
            List<LoanActiveDTO> overdueLoans = loanClient.getOverdueLoans();

            // Combinar todos los préstamos (activos + vencidos)
            List<LoanActiveDTO> allActiveLoans = new ArrayList<>();
            allActiveLoans.addAll(activeLoans);
            allActiveLoans.addAll(overdueLoans);

            return allActiveLoans.stream().map(loan -> {
                // Determinar estado
                String estado = "VIGENTE";
                Long daysOverdue = 0L;

                if (loan.getDueDate() != null && loan.getDueDate().isBefore(LocalDateTime.now())) {
                    daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDateTime.now());
                    estado = "ATRASADO (" + daysOverdue + " días)";
                }

                if (loan.getStatus() != null && loan.getStatus().contains("OVERDUE")) {
                    estado = "ATRASADO";
                }

                return new ActiveLoanReportDTO(
                        loan.getId(),
                        loan.getCustomerName(),
                        loan.getToolName(),
                        loan.getLoanDate(),
                        loan.getDueDate(),
                        daysOverdue,
                        loan.getFineAmount(),
                        estado
                );
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error generando reporte de préstamos activos", e);
            throw new RuntimeException("Error generando reporte: " + e.getMessage());
        }
    }

    // RF6.2: Listar clientes con atrasos
    public List<CustomerDelayReportDTO> getCustomersWithDelaysReport() {
        try {
            // Obtener préstamos vencidos
            List<LoanActiveDTO> overdueLoans = loanClient.getOverdueLoans();

            if (overdueLoans.isEmpty()) {
                return Collections.emptyList();
            }

            // Agrupar por cliente
            Map<Long, List<LoanActiveDTO>> loansByCustomer = overdueLoans.stream()
                    .collect(Collectors.groupingBy(this::extractCustomerIdFromLoan));

            // Para cada cliente con atrasos, obtener su información
            List<CustomerDelayReportDTO> result = new ArrayList<>();

            for (Map.Entry<Long, List<LoanActiveDTO>> entry : loansByCustomer.entrySet()) {
                Long customerId = entry.getKey();
                List<LoanActiveDTO> customerLoans = entry.getValue();

                try {
                    // Obtener información del cliente
                    CustomerModel customer = customerClient.getCustomer(customerId);

                    if (customer != null) {
                        // Calcular totales
                        Double totalFines = customerLoans.stream()
                                .mapToDouble(loan -> loan.getFineAmount() != null ? loan.getFineAmount() : 0.0)
                                .sum();

                        Long maxDaysOverdue = customerLoans.stream()
                                .mapToLong(loan -> {
                                    if (loan.getDueDate() != null && loan.getDueDate().isBefore(LocalDateTime.now())) {
                                        return ChronoUnit.DAYS.between(loan.getDueDate(), LocalDateTime.now());
                                    }
                                    return 0L;
                                })
                                .max()
                                .orElse(0L);

                        // Último préstamo atrasado
                        LocalDateTime lastOverdueDate = customerLoans.stream()
                                .map(LoanActiveDTO::getDueDate)
                                .max(LocalDateTime::compareTo)
                                .orElse(null);

                        result.add(new CustomerDelayReportDTO(
                                customerId,
                                customer.getName(),
                                customer.getRut(),
                                customer.getEmail(),
                                customerLoans.size(),
                                maxDaysOverdue,
                                totalFines,
                                "RESTRINGIDO", // Se asume restringido si tiene atrasos
                                lastOverdueDate
                        ));
                    }
                } catch (Exception e) {
                    log.warn("No se pudo obtener información del cliente {}", customerId, e);
                }
            }

            return result;

        } catch (Exception e) {
            log.error("Error generando reporte de clientes con atrasos", e);
            throw new RuntimeException("Error generando reporte: " + e.getMessage());
        }
    }

    // RF6.3: Reporte de herramientas más prestadas (Ranking)
    public List<MostBorrowedToolDTO> getMostBorrowedToolsReport(LocalDate startDate, LocalDate endDate) {
        try {
            // Para simplificar, usamos todos los préstamos que tenemos
            // En un sistema real, filtraríamos por fecha

            List<LoanActiveDTO> allLoans = new ArrayList<>();

            try {
                allLoans.addAll(loanClient.getActiveLoans());
            } catch (Exception e) {
                log.warn("Error obteniendo préstamos activos", e);
            }

            try {
                allLoans.addAll(loanClient.getOverdueLoans());
            } catch (Exception e) {
                log.warn("Error obteniendo préstamos vencidos", e);
            }

            // Contar préstamos por herramienta
            Map<String, Long> loanCountByTool = allLoans.stream()
                    .filter(loan -> loan.getToolName() != null && !loan.getToolName().equals("Desconocido"))
                    .collect(Collectors.groupingBy(
                            LoanActiveDTO::getToolName,
                            Collectors.counting()
                    ));

            // Obtener todas las herramientas para información adicional
            List<ToolGroupModel> allTools;
            try {
                allTools = toolClient.getAllToolGroups();
            } catch (Exception e) {
                log.warn("Error obteniendo herramientas, usando datos básicos", e);
                allTools = Collections.emptyList();
            }

            // Si no hay herramientas en la base, crear DTOs básicos
            if (allTools.isEmpty() && !loanCountByTool.isEmpty()) {
                return loanCountByTool.entrySet().stream()
                        .map(entry -> new MostBorrowedToolDTO(
                                null, // Sin ID
                                entry.getKey(),
                                "Desconocida",
                                entry.getValue(),
                                0L, // Stock desconocido
                                0.0 // Valor desconocido
                        ))
                        .sorted((t1, t2) -> t2.getLoanCount().compareTo(t1.getLoanCount()))
                        .limit(10)
                        .collect(Collectors.toList());
            }

            // Construir ranking con información completa
            return allTools.stream()
                    .map(tool -> {
                        Long loanCount = loanCountByTool.getOrDefault(tool.getName(), 0L);
                        return new MostBorrowedToolDTO(
                                tool.getId(),
                                tool.getName(),
                                tool.getCategory(),
                                loanCount,
                                tool.getAvailableCount(),
                                tool.getReplacementValue()
                        );
                    })
                    .sorted((t1, t2) -> t2.getLoanCount().compareTo(t1.getLoanCount()))
                    .limit(10)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error generando ranking de herramientas", e);
            throw new RuntimeException("Error generando ranking: " + e.getMessage());
        }
    }

    // Método auxiliar para extraer ID de cliente del préstamo
    private Long extractCustomerIdFromLoan(LoanActiveDTO loan) {
        try {
            // Asumiendo que customerName contiene el ID: "Nombre (ID: 123)"
            String customerName = loan.getCustomerName();
            if (customerName != null && customerName.contains("(ID:")) {
                String idPart = customerName.substring(
                        customerName.indexOf("(ID:") + 4,
                        customerName.indexOf(")")
                ).trim();
                return Long.parseLong(idPart);
            }
            // Si no está en el formato esperado, retornar null
            return null;
        } catch (Exception e) {
            log.warn("No se pudo extraer ID de cliente: {}", loan.getCustomerName(), e);
            return null;
        }
    }
}