// KardexView.jsx - ORDENAR EN FRONTEND
import { useEffect, useState } from "react";
import kardexService from "../services/kardex.service";
import toolGroupService from "../services/toolGroup.service";
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Stack,
  TextField,
  MenuItem,
  Button,
  Box,
  Chip,
} from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import dayjs from "dayjs";

const KardexView = () => {
  /* ---------- ESTADOS ---------- */
  const [rows, setRows] = useState([]);
  const [tools, setTools] = useState([]);
  const [selectedTool, setSelectedTool] = useState("");
  const [from, setFrom] = useState(dayjs().subtract(1, 'month'));
  const [to, setTo] = useState(dayjs());
  const [loading, setLoading] = useState(true);

  /* ---------- CARGAS INICIALES ---------- */
  useEffect(() => {
    loadAllMovements();
    loadTools();
  }, []);

  const loadAllMovements = async () => {
    setLoading(true);
    try {
      const res = await kardexService.getAllMovements();
      // ORDENAR por fecha descendente (más reciente primero)
      const sortedData = res.data.sort((a, b) => 
        new Date(b.movementDate) - new Date(a.movementDate)
      );
      setRows(sortedData);
    } catch (error) {
      console.error("Error loading kardex movements:", error);
    } finally {
      setLoading(false);
    }
  };

  const loadTools = async () => {
    try {
      const res = await toolGroupService.getAll();
      setTools(res.data);
    } catch (error) {
      console.error("Error loading tools:", error);
    }
  };

  /* ---------- FILTROS ---------- */
  const filterByTool = async () => {
    if (!selectedTool) return;
    setLoading(true);
    try {
      const res = await kardexService.getMovementsByToolGroup(selectedTool);
      const sortedData = res.data.sort((a, b) => 
        new Date(b.movementDate) - new Date(a.movementDate)
      );
      setRows(sortedData);
    } catch (error) {
      console.error("Error filtering by tool:", error);
    } finally {
      setLoading(false);
    }
  };

  const filterByRange = async () => {
    setLoading(true);
    try {
      const res = await kardexService.getMovementsByDateRange(
        from.format('YYYY-MM-DD'),
        to.format('YYYY-MM-DD')
      );
      const sortedData = res.data.sort((a, b) => 
        new Date(b.movementDate) - new Date(a.movementDate)
      );
      setRows(sortedData);
    } catch (error) {
      console.error("Error filtering by date:", error);
    } finally {
      setLoading(false);
    }
  };

  const clearFilters = () => {
    loadAllMovements();
    setSelectedTool("");
  };

  // Función para formatear fecha con hora
  const formatDateTime = (dateString) => {
    return dayjs(dateString).format('DD/MM/YYYY HH:mm:ss');
  };

  // Función para colores según tipo de movimiento
  const getMovementTypeColor = (type) => {
    const colors = {
      'REGISTRY': 'success',
      'LOAN': 'info',
      'RETURN': 'primary',
      'REPAIR': 'warning',
      'RETIRE': 'error',
      'RE_ENTRY': 'success'
    };
    return colors[type] || 'default';
  };

  /* ---------- RENDER ---------- */
  return (
    <Paper sx={{ p: 4, background: "#ffffff" }}>
      <Typography variant="h4" sx={{ mb: 3, color: "#6c63ff" }}>
        📋 Kardex de Movimientos
      </Typography>

      {/* ---------- FILTROS ---------- */}
      <Box sx={{ 
        p: 2, 
        mb: 3, 
        border: '1px solid #e0e0e0', 
        borderRadius: 2,
        backgroundColor: '#fafafa'
      }}>
        <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 'bold' }}>
          Filtros
        </Typography>
        
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} alignItems="flex-end">
          {/* Filtro por herramienta */}
          <TextField
            select
            label="Filtrar por Herramienta"
            value={selectedTool}
            onChange={(e) => setSelectedTool(e.target.value)}
            sx={{ minWidth: 250 }}
            size="small"
          >
            <MenuItem value="">Todas las herramientas</MenuItem>
            {tools.map((t) => (
              <MenuItem key={t.id} value={t.id}>
                {t.name} ({t.category})
              </MenuItem>
            ))}
          </TextField>

          {/* Filtro por fechas */}
          <LocalizationProvider dateAdapter={AdapterDayjs}>
            <Stack direction="row" spacing={1}>
              <DatePicker
                label="Desde"
                value={from}
                onChange={(newVal) => setFrom(newVal ?? dayjs())}
                slotProps={{ 
                  textField: { 
                    size: 'small',
                    sx: { minWidth: 150 }
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
                    sx: { minWidth: 150 }
                  } 
                }}
              />
            </Stack>
          </LocalizationProvider>

          {/* Botones de acción */}
          <Stack direction="row" spacing={1}>
            <Button 
              variant="contained" 
              onClick={filterByTool}
              disabled={!selectedTool}
              size="small"
            >
              Filtrar por Herramienta
            </Button>
            <Button 
              variant="contained" 
              onClick={filterByRange}
              size="small"
              color="secondary"
            >
              Filtrar por Fechas
            </Button>
            <Button 
              variant="outlined" 
              onClick={clearFilters}
              size="small"
            >
              Limpiar Filtros
            </Button>
          </Stack>
        </Stack>
      </Box>

      {/* ---------- TABLA ---------- */}
      <TableContainer sx={{ maxHeight: '70vh' }}>
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
              <TableCell>#</TableCell>
              <TableCell>Fecha y Hora</TableCell>
              <TableCell>Tipo</TableCell>
              <TableCell>Herramienta</TableCell>
              <TableCell>Usuario</TableCell>
              <TableCell>Detalles</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 3 }}>
                  Cargando movimientos...
                </TableCell>
              </TableRow>
            ) : rows.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 3 }}>
                  No hay movimientos registrados
                </TableCell>
              </TableRow>
            ) : (
              rows.map((r) => (
                <TableRow 
                  key={r.id} 
                  hover 
                  sx={{ 
                    '&:hover': { backgroundColor: '#f5f0ff' },
                    '&:nth-of-type(even)': { backgroundColor: '#f9f9f9' }
                  }}
                >
                  <TableCell sx={{ fontWeight: 'bold' }}>
                    {r.id}
                  </TableCell>
                  <TableCell sx={{ fontFamily: 'monospace' }}>
                    {formatDateTime(r.movementDate)}
                  </TableCell>
                  <TableCell>
                    <Chip 
                      label={r.movementType} 
                      size="small"
                      color={getMovementTypeColor(r.movementType)}
                      sx={{ 
                        fontWeight: 'bold',
                        minWidth: 90,
                        fontSize: '0.75rem'
                      }}
                    />
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                      {r.toolGroupName || 'Desconocido'}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      ID: {r.toolUnitId}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Box sx={{ 
                      backgroundColor: '#e3f2fd', 
                      px: 1, 
                      py: 0.5, 
                      borderRadius: 1,
                      display: 'inline-block'
                    }}>
                      {r.userName || 'Sistema'}
                    </Box>
                  </TableCell>
                  <TableCell sx={{ maxWidth: 400 }}>
                    <Typography 
                      variant="body2" 
                      sx={{ 
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap'
                      }}
                      title={r.details}
                    >
                      {r.details}
                    </Typography>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Contador de registros */}
      <Box sx={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        mt: 2, 
        pt: 2, 
        borderTop: '1px solid #e0e0e0' 
      }}>
        <Typography variant="body2" color="text.secondary">
          Mostrando {rows.length} movimientos
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Última actualización: {dayjs().format('DD/MM/YYYY HH:mm')}
        </Typography>
      </Box>
    </Paper>
  );
};

export default KardexView;