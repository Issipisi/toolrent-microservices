import { useState } from "react";
import reportService from "../services/report.service";
import {
  Button, Paper, Table, TableContainer, TableHead, TableRow, TableCell, TableBody, Typography, Stack, TextField,
} from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import dayjs from "dayjs";

const ReportView = () => {
  const [data, setData] = useState([]);
  const [title, setTitle] = useState("");

  /* ---------- FECHOS (solo para activos y top-tools) ---------- */
  const [from, setFrom] = useState(dayjs().subtract(1, 'month'));
  const [to, setTo] = useState(dayjs());

  /* ---------- LLAMADAS ---------- */
  const loadActive = async () => {
    try {
      const res = await reportService.getActiveLoansReport();
      setData(res.data);
      setTitle("Préstamos Activos");
    } catch (error) {
      console.error('Error loading active loans:', error);
      alert('Error al cargar préstamos activos');
    }
  };

  // Agregar nueva función
  const loadToolRanking = async () => {
    try {
      const res = await reportService.getToolRanking();
      setData(res.data);
      setTitle("Ranking de Herramientas por Préstamos");
    } catch (error) {
      console.error('Error loading tool ranking:', error);
      alert('Error al cargar ranking de herramientas: ' + (error.response?.data || error.message));
      setData([]);
    }
  };

  const loadCustomersWithDebt = async () => {
    try {
      console.log("=== Loading customers with debt ===");
      const res = await reportService.getCustomersWithDelaysReport();
      console.log("Customers with debt response:", res.data);
      
      if (!res.data || res.data.length === 0) {
        console.log("No customers with debt found");
        setData([]);
        setTitle("Clientes con deudas (0 encontrados)");
      } else {
        console.log("Primero cliente:", res.data[0]);
        console.log("Propiedades del primer cliente:", Object.keys(res.data[0]));
        
        setData(res.data);
        setTitle("Clientes con deudas");
      }
    } catch (error) {
      console.error('Error loading customers with debts:', error);
      setData([]);
      setTitle("Error al cargar clientes con deudas");
    }
  };

  /* ---------- RENDER ---------- */
  return (
    <Paper sx={{ p: 4, background: "#ffffff" }}>
      <Typography variant="h4" sx={{ mb: 3, color: "#6c63ff" }}>
        Reports
      </Typography>

      {/* Selector de fechas (solo para activos y top-tools) */}
      <LocalizationProvider dateAdapter={AdapterDayjs}>
        <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
          <DatePicker
            label="Desde"
            value={from}
            onChange={(newVal) => setFrom(newVal ?? dayjs())}
            slotProps={{ textField: { size: 'small' } }}
          />
          <DatePicker
            label="Hasta"
            value={to}
            onChange={(newVal) => setTo(newVal ?? dayjs())}
            slotProps={{ textField: { size: 'small' } }}
          />
        </Stack>
      </LocalizationProvider>

      <Stack direction="row" spacing={2} sx={{ mb: 3 }}>
        <Button variant="outlined" onClick={loadActive}>Préstamos Activos</Button>
        <Button variant="outlined" onClick={loadCustomersWithDebt}>Clientes con Deudas</Button>
        <Button variant="outlined" onClick={loadToolRanking}>Ranking por Préstamos</Button>
      </Stack>

      {data.length > 0 && (
        <>
          <Typography variant="h6" sx={{ mb: 2 }}>{title}</Typography>
          <TableContainer>
            <Table>
              <TableHead sx={{ background: "#f5f0ff" }}>
                <TableRow>
                  {title === "Préstamos Activos" && (
                    <>
                      <TableCell sx={{ color: "#2e2e4e" }}>Cliente</TableCell>
                      <TableCell sx={{ color: "#2e2e4e" }}>Herramienta</TableCell>
                      <TableCell sx={{ color: "#2e2e4e" }}>Fecha Préstamo</TableCell>
                      <TableCell sx={{ color: "#2e2e4e" }}>Fecha Devolución</TableCell>
                      <TableCell sx={{ color: "#2e2e4e" }}>Estado</TableCell>
                    </>
                  )}
                  {title === "Clientes con deudas" && (
                    <>
                      <TableCell>Nombre</TableCell>
                      <TableCell>Deuda ($)</TableCell>
                      <TableCell>Préstamos vencidos</TableCell>
                      <TableCell>Días de atraso</TableCell>
                    </>
                  )}
                  {title === "Ranking de Herramientas por Préstamos" && (
                    <>
                      <TableCell sx={{ color: "#2e2e4e" }}>Herramienta</TableCell>
                      <TableCell sx={{ color: "#2e2e4e" }}>Categoría</TableCell>
                      <TableCell sx={{ color: "#2e2e4e" }}>N° Préstamos</TableCell>
                      <TableCell sx={{ color: "#2e2e4e" }}>Stock Disponible</TableCell>
                      <TableCell sx={{ color: "#2e2e4e" }}>Valor Reposición</TableCell>
                    </>
                  )}
                </TableRow>
              </TableHead>
              <TableBody>
                {data.map((row, idx) => (
                  <TableRow key={idx} hover sx={{ "&:hover": { background: "#f5f0ff" } }}>
                    {title === "Préstamos Activos" && (
                      <>
                        <TableCell>{row.customerName}</TableCell>
                        <TableCell>{row.toolName}</TableCell>
                        <TableCell>{row.loanDate}</TableCell>
                        <TableCell>{row.dueDate}</TableCell>
                        <TableCell>{row.status}</TableCell>
                      </>
                    )}
                    {title === "Clientes con deudas" && (
                      <>
                        <TableCell>{row.customerName}</TableCell>
                        <TableCell>{row.totalDebt?.toLocaleString() || 0}</TableCell>
                        <TableCell>{row.overdueLoansCount || 0}</TableCell>
                        <TableCell>{row.maxDaysOverdue || 0}</TableCell>
                      </>
                    )}
                    {title === "Ranking de Herramientas por Préstamos" && (
                      <>
                        <TableCell>{row.toolName}</TableCell>
                        <TableCell>{row.category}</TableCell>
                        <TableCell>
                          <span style={{ fontWeight: 'bold', color: '#1976d2' }}>
                            {row.loanCount}
                          </span>
                        </TableCell>
                        <TableCell>{row.availableStock}</TableCell>
                        <TableCell>${row.replacementValue?.toLocaleString()}</TableCell>
                      </>
                    )}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      )}
    </Paper>
  );
};

export default ReportView;