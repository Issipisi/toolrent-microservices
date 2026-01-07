import React, { useEffect, useState } from "react";
import keycloak from "../services/keycloak";
import loanService from "../services/loan.service";
import customerService from "../services/customer.service";
import toolGroupService from "../services/toolGroup.service";
import {
  Box, Button, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField, Typography,
  MenuItem, Stack, Chip, Alert, Grid, Card, CardContent,
  IconButton, Tooltip
} from "@mui/material";
import { DateTimePicker } from "@mui/x-date-pickers/DateTimePicker";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import dayjs from "dayjs";
import AddIcon from '@mui/icons-material/Add';
import RefreshIcon from '@mui/icons-material/Refresh';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import PaidIcon from '@mui/icons-material/Paid';
import WarningIcon from '@mui/icons-material/Warning';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import LocalActivityIcon from '@mui/icons-material/LocalActivity';
import ScheduleIcon from '@mui/icons-material/Schedule';
import PeopleIcon from '@mui/icons-material/People';
import BuildIcon from '@mui/icons-material/Build';

const LoanView = () => {
  const [loans, setLoans] = useState([]);
  const [debts, setDebts] = useState([]);
  const [open, setOpen] = useState(false);
  const [openPreReturn, setOpenPreReturn] = useState(false);
  const [preDamageAmount, setPreDamageAmount] = useState("0");
  const [preIrreparable, setPreIrreparable] = useState(false);
  const [preDamageType, setPreDamageType] = useState("none");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  
  const userName = keycloak.tokenParsed?.preferred_username || keycloak.tokenParsed?.email || "Sistema";

  const [customers, setCustomers] = useState([]);
  const [tools, setTools] = useState([]);
  const [form, setForm] = useState({
    customerId: "",
    toolGroupId: "",
    dueDate: dayjs().add(7, 'day'),
  });

  /* ---------- CARGAS ---------- */
  const loadAll = async () => {
    setLoading(true);
    try {
      await Promise.all([
        loadActive(),
        loadPendingPayment(),
        loadCustomers(),
        loadTools()
      ]);
    } catch (error) {
      setMessage({ type: 'error', text: 'Error cargando datos' });
    } finally {
      setLoading(false);
    }
  };

  const loadActive = async () => {
    try {
      const res = await loanService.getActiveLoans();
      setLoans(res.data || []);
    } catch (error) {
      console.error("Error cargando préstamos activos:", error);
      setLoans([]);
    }
  };

  const loadPendingPayment = async () => {
    try {
      const res = await loanService.getReturnedWithDebts();
      console.log("📊 Préstamos con deudas:", res.data);
      setDebts(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      console.error("❌ Error cargando préstamos con deudas:", e);
      setDebts([]);
    }
  };

  const loadCustomers = async () => {
    try {
      const res = await customerService.getActive();
      setCustomers(res.data || []);
    } catch (error) {
      console.error("Error cargando clientes:", error);
      setCustomers([]);
    }
  };

  const loadTools = async () => {
    try {
      const res = await toolGroupService.getAvailableToolGroups();
      setTools(res.data || []);
    } catch (error) {
      console.error("Error cargando herramientas:", error);
      setTools([]);
    }
  };

  useEffect(() => {
    loadAll();
    
    const handleDebtUpdate = () => {
      loadPendingPayment();
    };
    window.addEventListener("debtUpdated", handleDebtUpdate);

    return () => window.removeEventListener("debtUpdated", handleDebtUpdate);
  }, []);

  /* ---------- HANDLERS ---------- */
  const handleRegister = async () => {
    try {
      await loanService.register({
        toolGroupId: form.toolGroupId,
        customerId: form.customerId,
        dueDate: form.dueDate.format("YYYY-MM-DDTHH:mm:ss")
      }, userName);
      
      setMessage({ type: 'success', text: '✅ Préstamo registrado exitosamente' });
      setOpen(false);
      setForm({ customerId: "", toolGroupId: "", dueDate: dayjs().add(7, 'day') });
      loadAll();
      setTimeout(() => setMessage({ type: '', text: '' }), 3000);
    } catch (e) {
      const msg = e.response?.data?.message || e.response?.data || "Error desconocido";
      setMessage({ type: 'error', text: `❌ No se puede registrar: ${msg}` });
    }
  };

  const handlePreReturn = (loanId) => {
    setOpenPreReturn(true);
    sessionStorage.setItem("pendingReturnId", loanId);
  };

  const handleDamageTypeChange = (e) => {
    const val = e.target.value;
    setPreDamageType(val);
    if (val === "none") {
      setPreIrreparable(false);
      setPreDamageAmount("0");
    } else if (val === "irreparable") {
      setPreIrreparable(true);
      setPreDamageAmount("0");
    } else {
      setPreIrreparable(false);
    }
  };

  const handleConfirmReturn = async () => {
    const loanId = sessionStorage.getItem("pendingReturnId");
    const amount = preIrreparable ? 0 : parseFloat(preDamageAmount);
    
    try {
      await loanService.returnLoan(loanId, amount, preIrreparable, userName);

      if (preDamageType === "leve" && amount > 0) {
        const loanRes = await loanService.getActive();
        const loan = loanRes.data.find(l => l.id == loanId);
        if (loan?.toolUnitId) {
          await loanService.sendToRepair(loan.toolUnitId);
        }
      }

      setMessage({ type: 'success', text: '✅ Devolución registrada exitosamente' });
      setOpenPreReturn(false);
      setPreDamageAmount("0");
      setPreIrreparable(false);
      setPreDamageType("none");
      loadAll();
      setTimeout(() => setMessage({ type: '', text: '' }), 3000);
    } catch (e) {
      setMessage({ type: 'error', text: `❌ Error en devolución: ${e.response?.data || "Error desconocido"}` });
    }
  };

  const handlePayDebts = async (loanId) => {
    if (!confirm("¿Marcar como pagadas las deudas de este préstamo?")) return;
    try {
      await loanService.payDebts(loanId);
      setMessage({ type: 'success', text: '✅ Deudas pagadas exitosamente' });
      loadPendingPayment();
      setTimeout(() => setMessage({ type: '', text: '' }), 3000);
    } catch (e) {
      setMessage({ type: 'error', text: `❌ Error pagando deudas: ${e.response?.data || "Error desconocido"}` });
    }
  };

  // Estadísticas
  const stats = {
    totalActive: loans.length,
    totalDebts: debts.length,
    totalDebtAmount: debts.reduce((sum, d) => sum + (d.fineAmount || 0) + (d.damageCharge || 0), 0),
    overdueLoans: loans.filter(l => dayjs(l.dueDate).isBefore(dayjs())).length
  };

  // Formatear fecha
  const formatDate = (dateString) => {
    return dayjs(dateString).format('DD/MM/YYYY HH:mm');
  };

  // Verificar si préstamo está vencido
  const isOverdue = (dueDate) => {
    return dayjs(dueDate).isBefore(dayjs());
  };

  return (
    <Paper sx={{ p: 4, background: "#ffffff" }}>
      {/* Encabezado */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" sx={{ color: "#6c63ff", display: 'flex', alignItems: 'center', gap: 1 }}>
            <LocalActivityIcon /> Gestión de Préstamos
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Administra préstamos, devoluciones y pagos de clientes
          </Typography>
        </Box>
        <Stack direction="row" spacing={1}>
          <Button 
            variant="outlined" 
            startIcon={<RefreshIcon />}
            onClick={loadAll}
            disabled={loading}
          >
            Actualizar
          </Button>
          <Button 
            variant="contained" 
            startIcon={<AddIcon />}
            onClick={() => setOpen(true)}
            sx={{ 
              background: "#6c63ff",
              "&:hover": { background: "#5a52d5" }
            }}
          >
            Nuevo Préstamo
          </Button>
        </Stack>
      </Box>

      {/* Mensajes */}
      {message.text && (
        <Alert severity={message.type} sx={{ mb: 3 }}>
          {message.text}
        </Alert>
      )}

      {/* Estadísticas */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={3}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" color="primary">
                {stats.totalActive}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Préstamos Activos
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} sm={3}>
          <Card sx={{ borderLeft: '4px solid #f44336' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" color="error.main">
                {stats.overdueLoans}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Préstamos Vencidos
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} sm={3}>
          <Card sx={{ borderLeft: '4px solid #ff9800' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" color="warning.main">
                {stats.totalDebts}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Con Deudas Pendientes
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} sm={3}>
          <Card sx={{ borderLeft: '4px solid #9c27b0' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" color="secondary.main">
                ${stats.totalDebtAmount.toLocaleString()}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Deuda Total
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* ---------- PRÉSTAMOS ACTIVOS ---------- */}
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
            <Typography variant="h5" sx={{ color: "#6c63ff", display: 'flex', alignItems: 'center', gap: 1 }}>
              <CheckCircleIcon /> Préstamos Activos
            </Typography>
            <Chip 
              label={`${loans.length} préstamos`} 
              color="primary" 
              size="small" 
            />
          </Box>
          
          <TableContainer sx={{ maxHeight: 400 }}>
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
                  <TableCell>ID</TableCell>
                  <TableCell>Cliente</TableCell>
                  <TableCell>Herramienta</TableCell>
                  <TableCell>Fecha Préstamo</TableCell>
                  <TableCell>Fecha Entrega</TableCell>
                  <TableCell>Estado</TableCell>
                  <TableCell>Acciones</TableCell>
                </TableRow>
              </TableHead>
              
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center" sx={{ py: 3 }}>
                      Cargando préstamos...
                    </TableCell>
                  </TableRow>
                ) : loans.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center" sx={{ py: 3 }}>
                      No hay préstamos activos
                    </TableCell>
                  </TableRow>
                ) : (
                  loans.map((loan) => (
                    <TableRow 
                      key={loan.id} 
                      hover 
                      sx={{ 
                        '&:hover': { backgroundColor: '#f5f0ff' },
                        '&:nth-of-type(even)': { backgroundColor: '#f9f9f9' }
                      }}
                    >
                      <TableCell sx={{ fontWeight: 'bold' }}>
                        #{loan.id}
                      </TableCell>
                      
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <PeopleIcon fontSize="small" color="action" />
                          <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                            {loan.customerName}
                          </Typography>
                        </Box>
                      </TableCell>
                      
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <BuildIcon fontSize="small" color="action" />
                          <Typography variant="body2">
                            {loan.toolName}
                          </Typography>
                        </Box>
                      </TableCell>
                      
                      <TableCell sx={{ fontFamily: 'monospace' }}>
                        {formatDate(loan.loanDate)}
                      </TableCell>
                      
                      <TableCell sx={{ fontFamily: 'monospace' }}>
                        <Box sx={{ 
                          display: 'flex', 
                          alignItems: 'center', 
                          gap: 1,
                          color: isOverdue(loan.dueDate) ? '#f44336' : 'inherit'
                        }}>
                          <ScheduleIcon fontSize="small" />
                          {formatDate(loan.dueDate)}
                        </Box>
                      </TableCell>
                      
                      <TableCell>
                        <Chip 
                          label={isOverdue(loan.dueDate) ? "VENCIDO" : "ACTIVO"} 
                          size="small"
                          color={isOverdue(loan.dueDate) ? "error" : "success"}
                          icon={isOverdue(loan.dueDate) ? <WarningIcon /> : <CheckCircleIcon />}
                        />
                      </TableCell>
                      
                      <TableCell>
                        <Tooltip title="Registrar Devolución">
                          <Button
                            size="small"
                            variant="outlined"
                            startIcon={<ArrowForwardIcon />}
                            onClick={() => handlePreReturn(loan.id)}
                            sx={{ textTransform: 'none' }}
                          >
                            Devolver
                          </Button>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      {/* ---------- PENDIENTES DE PAGO ---------- */}
      <Card>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
            <Typography variant="h5" sx={{ color: "#f44336", display: 'flex', alignItems: 'center', gap: 1 }}>
              <WarningIcon /> Pendientes de Pago
            </Typography>
            <Chip 
              label={`${debts.length} con deudas`} 
              color="error" 
              size="small" 
            />
          </Box>
          
          <TableContainer sx={{ maxHeight: 400 }}>
            <Table stickyHeader>
              <TableHead>
                <TableRow sx={{ 
                  '& th': { 
                    backgroundColor: '#f44336', 
                    color: 'white',
                    fontWeight: 'bold',
                    fontSize: '0.95rem'
                  }
                }}>
                  <TableCell>ID</TableCell>
                  <TableCell>Cliente</TableCell>
                  <TableCell>Herramienta</TableCell>
                  <TableCell>Fecha Devolución</TableCell>
                  <TableCell>Multa</TableCell>
                  <TableCell>Daño</TableCell>
                  <TableCell>Total</TableCell>
                  <TableCell>Acciones</TableCell>
                </TableRow>
              </TableHead>
              
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={8} align="center" sx={{ py: 3 }}>
                      Cargando deudas...
                    </TableCell>
                  </TableRow>
                ) : debts.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={8} align="center" sx={{ py: 3 }}>
                      No hay préstamos con deudas pendientes
                    </TableCell>
                  </TableRow>
                ) : (
                  debts.map((debt) => {
                    const totalDebt = (debt.fineAmount || 0) + (debt.damageCharge || 0);
                    return (
                      <TableRow 
                        key={debt.id} 
                        hover 
                        sx={{ 
                          '&:hover': { backgroundColor: '#ffebee' },
                          '&:nth-of-type(even)': { backgroundColor: '#fce4ec' }
                        }}
                      >
                        <TableCell sx={{ fontWeight: 'bold' }}>
                          #{debt.id}
                        </TableCell>
                        
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <PeopleIcon fontSize="small" color="action" />
                            <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                              {debt.customerName}
                            </Typography>
                          </Box>
                        </TableCell>
                        
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <BuildIcon fontSize="small" color="action" />
                            <Typography variant="body2">
                              {debt.toolName}
                            </Typography>
                          </Box>
                        </TableCell>
                        
                        <TableCell sx={{ fontFamily: 'monospace' }}>
                          {debt.returnDate ? formatDate(debt.returnDate) : 'No devuelto'}
                        </TableCell>
                        
                        <TableCell sx={{ fontFamily: 'monospace' }}>
                          {debt.fineAmount ? (
                            <Box sx={{ 
                              backgroundColor: '#fff3e0', 
                              px: 1, 
                              py: 0.5, 
                              borderRadius: 1,
                              display: 'inline-block'
                            }}>
                              ${debt.fineAmount.toLocaleString()}
                            </Box>
                          ) : (
                            <Chip label="Sin multa" size="small" color="default" />
                          )}
                        </TableCell>
                        
                        <TableCell sx={{ fontFamily: 'monospace' }}>
                          {debt.damageCharge ? (
                            <Box sx={{ 
                              backgroundColor: '#ffebee', 
                              px: 1, 
                              py: 0.5, 
                              borderRadius: 1,
                              display: 'inline-block'
                            }}>
                              ${debt.damageCharge.toLocaleString()}
                            </Box>
                          ) : (
                            <Chip label="Sin daño" size="small" color="default" />
                          )}
                        </TableCell>
                        
                        <TableCell>
                          <Typography variant="body1" sx={{ 
                            fontWeight: 'bold', 
                            color: totalDebt > 0 ? '#f44336' : '#4caf50',
                            fontFamily: 'monospace'
                          }}>
                            ${totalDebt.toLocaleString()}
                          </Typography>
                        </TableCell>
                        
                        <TableCell>
                          <Tooltip title="Marcar como pagado">
                            <Button
                              size="small"
                              variant="contained"
                              color="success"
                              startIcon={<PaidIcon />}
                              onClick={() => handlePayDebts(debt.id)}
                              sx={{ textTransform: 'none' }}
                            >
                              Pagar
                            </Button>
                          </Tooltip>
                        </TableCell>
                      </TableRow>
                    );
                  })
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      {/* Modal Nuevo Préstamo */}
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ 
          background: "linear-gradient(135deg, #6c63ff 0%, #9d4edd 100%)", 
          color: 'white',
          display: 'flex',
          alignItems: 'center',
          gap: 1
        }}>
          <AddIcon /> Nuevo Préstamo
        </DialogTitle>
        
        <DialogContent sx={{ mt: 2 }}>
          <Stack spacing={3}>
            <TextField 
              select 
              label="Cliente"
              value={form.customerId}
              onChange={(e) => setForm({ ...form, customerId: e.target.value })}
              fullWidth
              required
            >
              <MenuItem value="">
                -- Seleccionar cliente --
              </MenuItem>
              {customers
                .filter((c) => c.name !== "Sistema")
                .map((c) => (
                  <MenuItem key={c.id} value={c.id}>
                    {c.name} ‑ {c.rut}
                  </MenuItem>
                ))}
            </TextField>
            
            <TextField 
              select 
              label="Grupo de Herramientas"
              value={form.toolGroupId}
              onChange={(e) => setForm({ ...form, toolGroupId: e.target.value })}
              fullWidth
              required
            >
              <MenuItem value="">
                -- Seleccionar herramienta --
              </MenuItem>
              {tools.map((t) => (
                <MenuItem key={t.id} value={t.id}>
                  {t.name} ({t.category}) - Stock: {t.availableCount}
                </MenuItem>
              ))}
            </TextField>
            
            <LocalizationProvider dateAdapter={AdapterDayjs}>
              <DateTimePicker
                label="Fecha de Entrega Pactada"
                value={form.dueDate}
                onChange={(newVal) => setForm({ ...form, dueDate: newVal })}
                minDateTime={dayjs().add(1, 'hour')}
                slotProps={{ 
                  textField: { 
                    fullWidth: true,
                    helperText: "Fecha límite para la devolución"
                  } 
                }}
              />
            </LocalizationProvider>
          </Stack>
        </DialogContent>
        
        <DialogActions>
          <Button onClick={() => setOpen(false)} sx={{ color: "#666" }}>
            Cancelar
          </Button>
          <Button 
            onClick={handleRegister} 
            variant="contained"
            disabled={!form.customerId || !form.toolGroupId}
            sx={{ 
              background: "#6c63ff",
              "&:hover": { background: "#5a52d5" }
            }}
          >
            Registrar Préstamo
          </Button>
        </DialogActions>
      </Dialog>

      {/* Modal Devolución */}
      <Dialog open={openPreReturn} onClose={() => setOpenPreReturn(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ 
          background: "linear-gradient(135deg, #4caf50 0%, #2e7d32 100%)", 
          color: 'white',
          display: 'flex',
          alignItems: 'center',
          gap: 1
        }}>
          <ArrowForwardIcon /> Registrar Devolución
        </DialogTitle>
        
        <DialogContent sx={{ mt: 2 }}>
          <Stack spacing={3}>
            <Typography variant="body1">
              ¿La herramienta fue devuelta con daños?
            </Typography>
            
            <TextField
              select
              label="Estado de la Herramienta"
              value={preDamageType}
              onChange={handleDamageTypeChange}
              fullWidth
            >
              <MenuItem value="none">
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <CheckCircleIcon color="success" />
                  Sin daño - Estado normal
                </Box>
              </MenuItem>
              <MenuItem value="leve">
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <WarningIcon color="warning" />
                  Daño leve - Requiere reparación
                </Box>
              </MenuItem>
              <MenuItem value="irreparable">
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <WarningIcon color="error" />
                  Daño irreparable - Requiere baja
                </Box>
              </MenuItem>
            </TextField>
            
            {preDamageType === "leve" && (
              <TextField
                label="Monto del daño leve ($)"
                type="number"
                value={preDamageAmount}
                onChange={(e) => setPreDamageAmount(e.target.value)}
                fullWidth
                helperText="Costo estimado de reparación"
              />
            )}
            
            {preDamageType === "irreparable" && (
              <Alert severity="warning">
                La herramienta será dada de baja y se cobrará el valor de reposición al cliente.
              </Alert>
            )}
          </Stack>
        </DialogContent>
        
        <DialogActions>
          <Button onClick={() => setOpenPreReturn(false)} sx={{ color: "#666" }}>
            Cancelar
          </Button>
          <Button 
            onClick={handleConfirmReturn} 
            variant="contained"
            color="primary"
            sx={{ 
              background: preDamageType === "irreparable" ? "#f44336" : 
                        preDamageType === "leve" ? "#ff9800" : "#4caf50"
            }}
          >
            {preDamageType === "irreparable" ? "Registrar Baja" : 
             preDamageType === "leve" ? "Registrar con Daño" : "Registrar Devolución"}
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
};

export default LoanView;