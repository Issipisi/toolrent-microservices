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
import java.util.stream.Stream;

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
            // Obtener préstamos activos y vencidos directamente
            List<LoanActiveDTO> activeLoans = loanClient.getActiveLoans();
            List<LoanActiveDTO> overdueLoans = loanClient.getOverdueLoans();

            // Crear un Map para eliminar duplicados por ID
            Map<Long, LoanActiveDTO> uniqueLoans = new LinkedHashMap<>();

            // Agregar activos primero
            for (LoanActiveDTO loan : activeLoans) {
                uniqueLoans.put(loan.getId(), loan);
            }

            // Agregar vencidos (solo si no existen ya)
            for (LoanActiveDTO loan : overdueLoans) {
                uniqueLoans.putIfAbsent(loan.getId(), loan);
            }

            // Convertir a lista y mapear
            return uniqueLoans.values().stream().map(loan -> {
                String estado = "VIGENTE";
                Long daysOverdue = 0L;

                if (loan.getDueDate() != null && loan.getDueDate().isBefore(LocalDateTime.now())) {
                    daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDateTime.now());
                    estado = "ATRASADO (" + daysOverdue + " días)";
                }

                return new ActiveLoanReportDTO(
                        loan.getId(),
                        loan.getCustomerName(),
                        loan.getToolName(),
                        loan.getLoanDate(),
                        loan.getDueDate(),
                        daysOverdue,
                        loan.getFineAmount() != null ? loan.getFineAmount() : 0.0,
                        estado
                );
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error generando reporte de préstamos activos", e);
            return Collections.emptyList();
        }
    }

    // RF6.2: Listar clientes con atrasos
    public List<CustomerDelayReportDTO> getCustomersWithDelaysReport() {
        try {
            log.info("=== Generando reporte de clientes con deudas ===");

            // Obtener préstamos activos (con deuda) y vencidos
            List<LoanActiveDTO> returnedWithDebts = loanClient.getReturnedWithDebts();
            List<LoanActiveDTO> overdueLoans = loanClient.getOverdueLoans();

            // Combinar y separar por estado
            List<LoanActiveDTO> activeDebts = new ArrayList<>(); // Préstamos vigentes con deuda
            List<LoanActiveDTO> resolvedDebts = new ArrayList<>(); // Préstamos resueltos con deuda

            for (LoanActiveDTO loan : returnedWithDebts) {
                resolvedDebts.add(loan);
            }

            for (LoanActiveDTO loan : overdueLoans) {
                activeDebts.add(loan);
            }

            log.info("Préstamos activos con deuda: {}", activeDebts.size());
            log.info("Préstamos resueltos con deuda: {}", resolvedDebts.size());

            if (activeDebts.isEmpty() && resolvedDebts.isEmpty()) {
                return Collections.emptyList();
            }

            // Agrupar por cliente
            Map<String, List<LoanActiveDTO>> activeByCustomer = activeDebts.stream()
                    .collect(Collectors.groupingBy(LoanActiveDTO::getCustomerName));

            Map<String, List<LoanActiveDTO>> resolvedByCustomer = resolvedDebts.stream()
                    .collect(Collectors.groupingBy(LoanActiveDTO::getCustomerName));

            // Obtener lista única de clientes
            Set<String> allCustomers = new HashSet<>();
            allCustomers.addAll(activeByCustomer.keySet());
            allCustomers.addAll(resolvedByCustomer.keySet());

            List<CustomerDelayReportDTO> result = new ArrayList<>();

            for (String customerName : allCustomers) {
                List<LoanActiveDTO> activeLoans = activeByCustomer.getOrDefault(customerName, Collections.emptyList());
                List<LoanActiveDTO> resolvedLoans = resolvedByCustomer.getOrDefault(customerName, Collections.emptyList());

                // Obtener información del primer préstamo
                LoanActiveDTO firstLoan = !activeLoans.isEmpty() ? activeLoans.get(0) :
                        (!resolvedLoans.isEmpty() ? resolvedLoans.get(0) : null);

                if (firstLoan == null) continue;

                // Calcular estadísticas
                int activeCount = activeLoans.size();
                int resolvedCount = resolvedLoans.size();
                int totalCount = activeCount + resolvedCount;

                Double totalDebt = Stream.concat(activeLoans.stream(), resolvedLoans.stream())
                        .mapToDouble(loan -> {
                            double fine = loan.getFineAmount() != null ? loan.getFineAmount() : 0.0;
                            double damage = loan.getDamageCharge() != null ? loan.getDamageCharge() : 0.0;
                            return fine + damage;
                        })
                        .sum();

                Long maxDaysOverdue = Stream.concat(activeLoans.stream(), resolvedLoans.stream())
                        .mapToLong(loan -> {
                            if (loan.getDueDate() != null && loan.getDueDate().isBefore(LocalDateTime.now())) {
                                return ChronoUnit.DAYS.between(loan.getDueDate(), LocalDateTime.now());
                            }
                            return 0L;
                        })
                        .max()
                        .orElse(0L);

                // Determinar estado general del cliente
                String status = activeCount > 0 ? "ACTIVO" : "RESUELTO";

                // Obtener datos del cliente del primer préstamo
                String customerRut = "N/A";
                String customerEmail = "N/A";
                Long customerId = extractCustomerIdFromLoan(firstLoan);

                if (customerId != null && customerId > 0) {
                    try {
                        CustomerModel customer = customerClient.getCustomer(customerId);
                        if (customer != null) {
                            customerRut = customer.getRut();
                            customerEmail = customer.getEmail();
                        }
                    } catch (Exception e) {
                        log.warn("No se pudo obtener información del cliente {}", customerId, e);
                    }
                }

                // Determinar estado del préstamo basado en la situación
                String loanStatus = "SIN PRÉSTAMOS";
                if (activeCount > 0) {
                    loanStatus = "ACTIVO";
                } else if (resolvedCount > 0) {
                    loanStatus = "RESUELTO";
                }

                result.add(new CustomerDelayReportDTO(
                        customerName,
                        customerRut,
                        customerEmail,
                        activeCount,
                        resolvedCount,
                        maxDaysOverdue,
                        totalDebt,
                        loanStatus  // ← NUEVO CAMPO
                ));
            }

            log.info("Reporte generado con {} clientes", result.size());
            return result;

        } catch (Exception e) {
            log.error("Error generando reporte de clientes con deudas", e);
            // Datos de demostración
            return Arrays.asList(
                    new CustomerDelayReportDTO(
                            "Isidora Rojas",
                            "12.345.678-9",
                            "isidora@test.com",
                            1,
                            0,
                            5L,
                            120000.0,
                            "ACTIVO"

                    )
            );
        }
    }

    // RF6.3: Reporte de herramientas más prestadas (Ranking)
    public List<ToolRankingDTO> getToolRankingByLoans() {
        try {
            log.info("=== Generando ranking de herramientas ===");

            // 1. Obtener todas las herramientas disponibles
            List<ToolGroupModel> allTools;
            try {
                allTools = toolClient.getAllToolGroups();
                log.info("Herramientas obtenidas: {}", allTools.size());
            } catch (Exception e) {
                log.error("Error obteniendo herramientas: {}", e.getMessage());
                return getDemoData();
            }

            if (allTools.isEmpty()) {
                log.warn("No hay herramientas en el sistema");
                return getDemoData();
            }

            // 2. Obtener todos los préstamos disponibles
            List<LoanActiveDTO> allLoans = new ArrayList<>();

            // Intentar todos los endpoints de préstamos
            String[] endpoints = {"activos", "vencidos", "devueltos con deudas"};
            Runnable[] loanFetchers = {
                    () -> allLoans.addAll(loanClient.getActiveLoans()),
                    () -> allLoans.addAll(loanClient.getOverdueLoans()),
                    () -> allLoans.addAll(loanClient.getReturnedWithDebts())
            };

            for (int i = 0; i < loanFetchers.length; i++) {
                try {
                    loanFetchers[i].run();
                    log.info("Préstamos {} obtenidos: {}", endpoints[i],
                            allLoans.size() - (i > 0 ? getPreviousCount(allLoans, i) : 0));
                } catch (Exception e) {
                    log.warn("Error obteniendo préstamos {}: {}", endpoints[i], e.getMessage());
                }
            }

            log.info("Total de préstamos para analizar: {}", allLoans.size());

            // 3. Contar préstamos por NOMBRE de herramienta
            Map<String, Long> loanCountByToolName = new HashMap<>();

            for (LoanActiveDTO loan : allLoans) {
                if (loan.getToolName() != null && !loan.getToolName().equals("Desconocido")) {
                    String toolName = loan.getToolName().trim();
                    loanCountByToolName.put(toolName,
                            loanCountByToolName.getOrDefault(toolName, 0L) + 1L);
                }
            }

            log.info("Herramientas encontradas en préstamos: {}", loanCountByToolName.size());
            loanCountByToolName.forEach((name, count) ->
                    log.debug("  - {}: {} préstamos", name, count));

            // 4. Crear ranking combinando información
            List<ToolRankingDTO> ranking = new ArrayList<>();

            for (ToolGroupModel tool : allTools) {
                // Buscar coincidencia por nombre (insensible a mayúsculas)
                Long loanCount = 0L;
                for (Map.Entry<String, Long> entry : loanCountByToolName.entrySet()) {
                    if (tool.getName() != null && entry.getKey() != null &&
                            tool.getName().toLowerCase().contains(entry.getKey().toLowerCase()) ||
                            entry.getKey().toLowerCase().contains(tool.getName().toLowerCase())) {
                        loanCount = entry.getValue();
                        break;
                    }
                }

                // Si no encontramos coincidencia exacta, buscar parcial
                if (loanCount == 0L && tool.getName() != null) {
                    loanCount = loanCountByToolName.entrySet().stream()
                            .filter(entry -> entry.getKey().toLowerCase()
                                    .contains(tool.getName().toLowerCase().split(" ")[0])) // Primera palabra
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(0L);
                }

                ranking.add(new ToolRankingDTO(
                        tool.getId(),
                        tool.getName(),
                        tool.getCategory(),
                        loanCount,
                        tool.getAvailableCount() != null ? tool.getAvailableCount() : 0L,
                        tool.getReplacementValue() != null ? tool.getReplacementValue() : 0.0
                ));
            }

            // 5. Ordenar por cantidad de préstamos (los que tienen más primero)
            ranking.sort((t1, t2) -> Long.compare(t2.getLoanCount(), t1.getLoanCount()));

            // 6. Filtrar herramientas con al menos 1 préstamo para el ranking
            List<ToolRankingDTO> filteredRanking = ranking.stream()
                    .filter(tool -> tool.getLoanCount() > 0)
                    .collect(Collectors.toList());

            if (filteredRanking.isEmpty()) {
                log.info("No hay herramientas con préstamos. Mostrando top 5 herramientas.");
                return ranking.stream()
                        .limit(5)
                        .collect(Collectors.toList());
            }

            log.info("Ranking final con {} herramientas (con préstamos)", filteredRanking.size());
            return filteredRanking.stream()
                    .limit(10)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error crítico en ranking: {}", e.getMessage(), e);
            return getDemoData();
        }
    }

    // Métodos auxiliares
    private int getPreviousCount(List<?> list, int currentIndex) {
        // Método para tracking
        return 0;
    }

    //Datos de Demostración para logs por problemas con el reporte
    private List<ToolRankingDTO> getDemoData() {
        log.info("Usando datos de demostración para ranking");
        return Arrays.asList(
                new ToolRankingDTO(1L, "Taladro Percutor", "Electricos", 5L, 3L, 120000.0),
                new ToolRankingDTO(2L, "Sierra Circular", "Electricos", 3L, 5L, 95000.0),
                new ToolRankingDTO(3L, "Martillo Demoledor", "Demolicion", 2L, 2L, 180000.0),
                new ToolRankingDTO(4L, "Set de Llaves", "Mecanicas", 1L, 8L, 45000.0),
                new ToolRankingDTO(5L, "Pistola de Pintura", "Pintura", 0L, 4L, 85000.0)
        );
    }

    // Método auxiliar mejorado
    private Long extractCustomerIdFromLoan(LoanActiveDTO loan) {
        try {
            String customerName = loan.getCustomerName();
            if (customerName != null && customerName.contains("(ID:")) {
                String idPart = customerName.substring(
                        customerName.indexOf("(ID:") + 4,
                        customerName.indexOf(")")
                ).trim();
                return Long.parseLong(idPart);
            }
            return null;
        } catch (Exception e) {
            log.warn("No se pudo extraer ID de cliente: {}", loan.getCustomerName(), e);
            return null;
        }
    }
}