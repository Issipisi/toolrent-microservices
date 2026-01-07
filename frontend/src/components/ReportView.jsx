import { useState } from "react";
import reportService from "../services/report.service";
import {
  Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Typography, Stack, Button, Box, Chip, Alert, CircularProgress,
  Card, CardContent, Grid
} from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import dayjs from "dayjs";
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import PeopleIcon from '@mui/icons-material/People';
import LocalActivityIcon from '@mui/icons-material/LocalActivity';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';

const ReportView = () => {
  const [data, setData] = useState([]);
  const [title, setTitle] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [from, setFrom] = useState(dayjs().subtract(1, 'month'));
  const [to, setTo] = useState(dayjs());

  const formatDate = (dateString) => {
    return dayjs(dateString).format('DD/MM/YYYY HH:mm');
  };

  const loadActive = async () => {
    setLoading(true);
    setError("");
    try {
      const res = await reportService.getActiveLoansReport();
      setData(res.data);
      setTitle("📋 Préstamos Activos");
    } catch (error) {
      setError("Error al cargar préstamos activos");
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadToolRanking = async () => {
    setLoading(true);
    setError("");
    try {
      const res = await reportService.getToolRanking();
      setData(res.data);
      setTitle("🏆 Ranking de Herramientas");
    } catch (error) {
      setError("Error al cargar ranking de herramientas");
      console.error('Error:', error);
      setData([]);
    } finally {
      setLoading(false);
    }
  };

  const loadCustomersWithDebt = async () => {
    setLoading(true);
    setError("");
    try {
      const res = await reportService.getCustomersWithDelaysReport();
      if (!res.data || res.data.length === 0) {
        setData([]);
        setTitle("Clientes con deudas (0 encontrados)");
      } else {
        setData(res.data);
        setTitle("⚠️ Clientes con Deudas Pendientes");
      }
    } catch (error) {
      setError("Error al cargar clientes con deudas");
      console.error('Error:', error);
      setData([]);
    } finally {
      setLoading(false);
    }
  };


  // Estadísticas rápidas
  const stats = {
    totalActiveLoans: data.filter(d => d.status === 'ACTIVE').length,
    totalDebt: data.reduce((sum, d) => sum + (d.totalDebt || 0), 0),
    maxLoanCount: data.length > 0 ? Math.max(...data.map(d => d.loanCount || 0)) : 0
  };

  return (
    <Paper sx={{ p: 4, background: "#ffffff" }}>
      <Typography variant="h4" sx={{ mb: 3, color: "#6c63ff" }}>
        📊 Reportes y Estadísticas
      </Typography>

      {/* Filtros de fecha */}
      <Box sx={{ 
        p: 3, 
        mb: 3, 
        border: '1px solid #e0e0e0', 
        borderRadius: 2,
        backgroundColor: '#fafafa'
      }}>
        <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: 1 }}>
          <CalendarMonthIcon /> Filtros de Fecha
        </Typography>
        
        <LocalizationProvider dateAdapter={AdapterDayjs}>
          <Stack direction="row" spacing={2} alignItems="center">
            <DatePicker
              label="Desde"
              value={from}
              onChange={(newVal) => setFrom(newVal ?? dayjs())}
              slotProps={{ 
                textField: { 
                  size: 'small',
                  sx: { minWidth: 200 }
                } 
              }}
            />
            <DatePicker
              label="Hasta"
              value={to}
              onChange={(newVal) => setTo(newVal ?? dayjs())}
              slotProps={{ 
                textField: { 
                  size: 'small',
                  sx: { minWidth: 200 }
                } 
              }}
            />
          </Stack>
        </LocalizationProvider>
      </Box>

      {/* Botones de reportes */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} md={4}>
          <Card 
            sx={{ 
              cursor: 'pointer',
              transition: 'transform 0.2s',
              '&:hover': { transform: 'translateY(-4px)' }
            }}
            onClick={loadActive}
          >
            <CardContent sx={{ textAlign: 'center' }}>
              <LocalActivityIcon sx={{ fontSize: 40, color: '#1976d2', mb: 1 }} />
              <Typography variant="h6">Préstamos Activos</Typography>
              <Typography variant="body2" color="text.secondary">
                Ver préstamos en curso
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} md={4}>
          <Card 
            sx={{ 
              cursor: 'pointer',
              transition: 'transform 0.2s',
              '&:hover': { transform: 'translateY(-4px)' }
            }}
            onClick={loadCustomersWithDebt}
          >
            <CardContent sx={{ textAlign: 'center' }}>
              <PeopleIcon sx={{ fontSize: 40, color: '#d32f2f', mb: 1 }} />
              <Typography variant="h6">Clientes con Deudas</Typography>
              <Typography variant="body2" color="text.secondary">
                Clientes con pagos pendientes
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} md={4}>
          <Card 
            sx={{ 
              cursor: 'pointer',
              transition: 'transform 0.2s',
              '&:hover': { transform: 'translateY(-4px)' }
            }}
            onClick={loadToolRanking}
          >
            <CardContent sx={{ textAlign: 'center' }}>
              <TrendingUpIcon sx={{ fontSize: 40, color: '#2e7d32', mb: 1 }} />
              <Typography variant="h6">Ranking Herramientas</Typography>
              <Typography variant="body2" color="text.secondary">
                Herramientas más solicitadas
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Errores */}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* Loading */}
      {loading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      )}

      {/* Datos */}
      {!loading && data.length > 0 && (
        <Box>
          {/* Encabezado con título y estadísticas */}
          <Box sx={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            mb: 3,
            p: 2,
            backgroundColor: '#f0f4ff',
            borderRadius: 2
          }}>
            <Typography variant="h5" sx={{ color: '#6c63ff' }}>
              {title}
            </Typography>
          </Box>

          {/* Tabla */}
          <TableContainer sx={{ maxHeight: '60vh' }}>
            <Table stickyHeader>
              <TableHead>
                <TableRow sx={{ 
                  '& th': { 
                    backgroundColor: '#6c63ff', 
                    color: 'white',
                    fontWeight: 'bold',
                    fontSize: '0.95rem'
                  }
                }}>
                  {title === "📋 Préstamos Activos" && (
                    <>
                      <TableCell>Cliente</TableCell>
                      <TableCell>Herramienta</TableCell>
                      <TableCell>Fecha Préstamo</TableCell>
                      <TableCell>Fecha Devolución</TableCell>
                      <TableCell>Estado</TableCell>
                    </>
                  )}
                  
                  {title === "⚠️ Clientes con Deudas Pendientes" && (
                    <>
                      <TableCell>Cliente</TableCell>
                      <TableCell>Deuda Total</TableCell>
                      <TableCell>Estado</TableCell>
                      <TableCell>Préstamos Vencidos</TableCell>
                      <TableCell>Días de Atraso</TableCell>
                    </>
                  )}
                  
                  {title === "🏆 Ranking de Herramientas" && (
                    <>
                      <TableCell>#</TableCell>
                      <TableCell>Herramienta</TableCell>
                      <TableCell>Categoría</TableCell>
                      <TableCell>Préstamos</TableCell>
                      <TableCell>Stock</TableCell>
                      <TableCell>Valor Reposición</TableCell>
                    </>
                  )}
                </TableRow>
              </TableHead>
              
              <TableBody>
                {data.map((row, idx) => (
                  <TableRow 
                    key={idx} 
                    hover 
                    sx={{ 
                      '&:hover': { backgroundColor: '#f5f0ff' },
                      '&:nth-of-type(even)': { backgroundColor: '#f9f9f9' }
                    }}
                  >
                    {title === "📋 Préstamos Activos" && (
                      <>
                        <TableCell sx={{ fontWeight: 'medium' }}>
                          {row.customerName}
                        </TableCell>
                        <TableCell>{row.toolName}</TableCell>
                        <TableCell sx={{ fontFamily: 'monospace' }}>
                          {formatDate(row.loanDate)}
                        </TableCell>
                        <TableCell sx={{ fontFamily: 'monospace' }}>
                          {formatDate(row.dueDate)}
                        </TableCell>
                        <TableCell>
                          <Chip 
                            label={row.status} 
                            size="small"
                            color={row.status === 'ACTIVE' ? 'success' : 'warning'}
                          />
                        </TableCell>
                      </>
                    )}
                    
                    {title === "⚠️ Clientes con Deudas Pendientes" && (
                      <>
                        <TableCell sx={{ fontWeight: 'medium' }}>
                          {row.customerName}
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2" color="error" fontWeight="bold">
                            ${(row.totalDebt || 0).toLocaleString()}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Chip 
                            label={row.loanStatus || "SIN ESTADO"} 
                            size="small"
                            color="error"
                          />
                        </TableCell>
                        <TableCell>
                          <Box sx={{ 
                            backgroundColor: '#ffebee', 
                            px: 1, 
                            py: 0.5, 
                            borderRadius: 1,
                            display: 'inline-block'
                          }}>
                            {row.overdueLoansCount || 0}
                          </Box>
                        </TableCell>
                        <TableCell>
                          {row.maxDaysOverdue || 0} días
                        </TableCell>
                      </>
                    )}
                    
                    {title === "🏆 Ranking de Herramientas" && (
                      <>
                        <TableCell>
                          <Box sx={{ 
                            backgroundColor: idx < 3 ? '#ffeb3b' : '#f5f5f5', 
                            width: 30, 
                            height: 30,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            borderRadius: '50%',
                            fontWeight: 'bold'
                          }}>
                            {idx + 1}
                          </Box>
                        </TableCell>
                        <TableCell sx={{ fontWeight: 'medium' }}>
                          {row.toolName}
                        </TableCell>
                        <TableCell>
                          <Chip 
                            label={row.category} 
                            size="small"
                            variant="outlined"
                          />
                        </TableCell>
                        <TableCell>
                          <Box sx={{ 
                            backgroundColor: '#e3f2fd', 
                            px: 2, 
                            py: 0.5, 
                            borderRadius: 1,
                            display: 'inline-block',
                            fontWeight: 'bold'
                          }}>
                            {row.loanCount}
                          </Box>
                        </TableCell>
                        <TableCell>
                          <Box sx={{ 
                            backgroundColor: row.availableStock > 0 ? '#e8f5e9' : '#ffebee', 
                            px: 2, 
                            py: 0.5, 
                            borderRadius: 1,
                            display: 'inline-block'
                          }}>
                            {row.availableStock} 
                          </Box>
                        </TableCell>
                        <TableCell sx={{ fontFamily: 'monospace' }}>
                          ${(row.replacementValue || 0).toLocaleString()}
                        </TableCell>
                      </>
                    )}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          {/* Pie de tabla */}
          <Box sx={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            mt: 2, 
            pt: 2, 
            borderTop: '1px solid #e0e0e0' 
          }}>
            <Typography variant="body2" color="text.secondary">
              Generado el {dayjs().format('DD/MM/YYYY HH:mm')}
            </Typography>
            <Button 
              variant="outlined" 
              size="small"
              onClick={() => window.print()}
            >
              📄 Imprimir Reporte
            </Button>
          </Box>
        </Box>
      )}

      {/* Sin datos */}
      {!loading && data.length === 0 && title && (
        <Box sx={{ 
          textAlign: 'center', 
          py: 8,
          backgroundColor: '#fafafa',
          borderRadius: 2
        }}>
          <Typography variant="h6" color="text.secondary">
            📭 No hay datos para mostrar
          </Typography>
        </Box>
      )}
    </Paper>
  );
};

export default ReportView;