import { useEffect, useState } from "react";
import customerService from "../services/customer.service";
import {
  Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Typography, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Box, Chip, Grid, Card, CardContent, Alert, IconButton,
  Stack, Avatar
} from "@mui/material";
import AddIcon from '@mui/icons-material/Add';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import PersonRemoveIcon from '@mui/icons-material/PersonRemove';
import EmailIcon from '@mui/icons-material/Email';
import PhoneIcon from '@mui/icons-material/Phone';
import BadgeIcon from '@mui/icons-material/Badge';
import PeopleIcon from '@mui/icons-material/People';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import EditIcon from '@mui/icons-material/Edit';
import RefreshIcon from '@mui/icons-material/Refresh';

const CustomerView = () => {
  const [customers, setCustomers] = useState([]);
  const [filteredCustomers, setFilteredCustomers] = useState([]);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [searchTerm, setSearchTerm] = useState("");
  const [form, setForm] = useState({ 
    name: "", 
    rut: "", 
    phone: "", 
    email: "" 
  });

  const loadCustomers = async () => {
    setLoading(true);
    try {
      const res = await customerService.getAll();
      const filtered = res.data.filter(c => c.email !== "system@toolrent.com");
      setCustomers(filtered);
      setFilteredCustomers(filtered);
    } catch (error) {
      setMessage({ type: 'error', text: 'Error cargando clientes' });
      console.error("Error:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async () => {
    try {
      await customerService.register({
        name: form.name,
        rut: form.rut,
        phone: form.phone,
        email: form.email
      });
      
      setMessage({ type: 'success', text: 'Cliente registrado exitosamente' });
      setOpen(false);
      setForm({ name: "", rut: "", phone: "", email: "" });
      loadCustomers();
      setTimeout(() => setMessage({ type: '', text: '' }), 3000);
    } catch (error) {
      setMessage({ 
        type: 'error', 
        text: error.response?.data || "Error al registrar cliente" 
      });
    }
  };

  const handleChangeStatus = async (id, newStatus) => {
    try {
      await customerService.updateStatus(id, newStatus);
      setMessage({ 
        type: 'success', 
        text: `Cliente ${newStatus === "ACTIVE" ? "activado" : "restringido"}` 
      });
      loadCustomers();
      setTimeout(() => setMessage({ type: '', text: '' }), 3000);
    } catch (error) {
      setMessage({ 
        type: 'error', 
        text: error.response?.data || "Error cambiando estado" 
      });
    }
  };

  // Filtrar clientes por búsqueda
  useEffect(() => {
    if (searchTerm.trim() === "") {
      setFilteredCustomers(customers);
    } else {
      const term = searchTerm.toLowerCase();
      const filtered = customers.filter(customer =>
        customer.name.toLowerCase().includes(term) ||
        customer.rut.toLowerCase().includes(term) ||
        customer.email.toLowerCase().includes(term) ||
        customer.phone.includes(term)
      );
      setFilteredCustomers(filtered);
    }
  }, [searchTerm, customers]);

  // Estadísticas
  const stats = {
    total: customers.length,
    active: customers.filter(c => c.status === 'ACTIVE').length,
    restricted: customers.filter(c => c.status === 'RESTRICTED').length,
    withLoans: customers.filter(c => c.hasActiveLoans).length
  };

  useEffect(() => {
    loadCustomers();
  }, []);

  const formatRUT = (rut) => {
    if (!rut) return "";
    return rut.replace(/(\d{1,2})(\d{3})(\d{3})([\dkK])$/, '$1.$2.$3-$4');
  };

  return (
    <Paper sx={{ p: 4, background: "#ffffff" }}>
      {/* Encabezado */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" sx={{ color: "#6c63ff", display: 'flex', alignItems: 'center', gap: 1 }}>
            <PeopleIcon /> Gestión de Clientes
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Administra la información de los clientes del sistema
          </Typography>
        </Box>
        <Button 
          variant="contained" 
          startIcon={<AddIcon />}
          onClick={() => setOpen(true)}
          sx={{ 
            background: "#6c63ff",
            "&:hover": { background: "#5a52d5" }
          }}
        >
          Nuevo Cliente
        </Button>
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
              <Typography variant="h4">{stats.total}</Typography>
              <Typography variant="body2" color="text.secondary">Clientes Totales</Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} sm={3}>
          <Card sx={{ borderLeft: '4px solid #4caf50' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" color="success.main">
                {stats.active}
              </Typography>
              <Typography variant="body2" color="text.secondary">Activos</Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} sm={3}>
          <Card sx={{ borderLeft: '4px solid #f44336' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" color="error.main">
                {stats.restricted}
              </Typography>
              <Typography variant="body2" color="text.secondary">Restringidos</Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} sm={3}>
          <Card sx={{ borderLeft: '4px solid #2196f3' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" color="info.main">
                {stats.withLoans}
              </Typography>
              <Typography variant="body2" color="text.secondary">Con Préstamos</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Búsqueda y filtros */}
      <Box sx={{ 
        p: 2, 
        mb: 3, 
        border: '1px solid #e0e0e0', 
        borderRadius: 2,
        backgroundColor: '#fafafa'
      }}>
        <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 'bold' }}>
          Buscar Cliente
        </Typography>
        
        <TextField
          fullWidth
          placeholder="Buscar por nombre, RUT, email o teléfono..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          sx={{ mb: 2 }}
        />
        
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="body2" color="text.secondary">
            {filteredCustomers.length} clientes encontrados
          </Typography>
          <Button 
            startIcon={<RefreshIcon />} 
            onClick={loadCustomers}
            size="small"
            disabled={loading}
          >
            Actualizar
          </Button>
        </Box>
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
              <TableCell>Cliente</TableCell>
              <TableCell>Contacto</TableCell>
              <TableCell>Estado</TableCell>
              <TableCell>Préstamos</TableCell>
              <TableCell>Acciones</TableCell>
            </TableRow>
          </TableHead>
          
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ py: 3 }}>
                  Cargando clientes...
                </TableCell>
              </TableRow>
            ) : filteredCustomers.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ py: 3 }}>
                  {searchTerm ? 'No se encontraron clientes' : 'No hay clientes registrados'}
                </TableCell>
              </TableRow>
            ) : (
              filteredCustomers.map((c) => (
                <TableRow 
                  key={c.id} 
                  hover 
                  sx={{ 
                    '&:hover': { backgroundColor: '#f5f0ff' },
                    '&:nth-of-type(even)': { backgroundColor: '#f9f9f9' }
                  }}
                >
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                      <Avatar sx={{ 
                        bgcolor: c.status === 'ACTIVE' ? '#4caf50' : '#f44336',
                        width: 40, 
                        height: 40 
                      }}>
                        {c.name.charAt(0).toUpperCase()}
                      </Avatar>
                      <Box>
                        <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                          {c.name}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          <BadgeIcon fontSize="small" sx={{ mr: 0.5, fontSize: '0.8rem' }} />
                          {formatRUT(c.rut)}
                        </Typography>
                      </Box>
                    </Box>
                  </TableCell>
                  
                  <TableCell>
                    <Stack spacing={0.5}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                        <EmailIcon fontSize="small" color="action" />
                        <Typography variant="body2">
                          {c.email}
                        </Typography>
                      </Box>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                        <PhoneIcon fontSize="small" color="action" />
                        <Typography variant="body2">
                          {c.phone}
                        </Typography>
                      </Box>
                    </Stack>
                  </TableCell>
                  
                  <TableCell>
                    <Chip 
                      label={c.status === 'ACTIVE' ? 'ACTIVO' : 'RESTRINGIDO'} 
                      size="small"
                      color={c.status === 'ACTIVE' ? 'success' : 'error'}
                      icon={c.status === 'ACTIVE' ? <CheckCircleIcon /> : <BlockIcon />}
                      sx={{ fontWeight: 'bold', minWidth: 100 }}
                    />
                  </TableCell>
                  
                  <TableCell>
                    <Box sx={{ 
                      backgroundColor: c.hasActiveLoans ? '#e3f2fd' : '#f5f5f5', 
                      px: 2, 
                      py: 0.5, 
                      borderRadius: 1,
                      display: 'inline-block',
                      fontSize: '0.85rem'
                    }}>
                      {c.activeLoansCount || 0} préstamos
                    </Box>
                  </TableCell>
                  
                  <TableCell>
                    <Stack direction="row" spacing={1}>
                      <Button
                        size="small"
                        variant="outlined"
                        color={c.status === 'ACTIVE' ? 'error' : 'success'}
                        startIcon={c.status === 'ACTIVE' ? <PersonRemoveIcon /> : <PersonAddIcon />}
                        onClick={() => handleChangeStatus(c.id, c.status === 'ACTIVE' ? 'RESTRICTED' : 'ACTIVE')}
                        sx={{ textTransform: 'none' }}
                      >
                        {c.status === 'ACTIVE' ? 'Restringir' : 'Activar'}
                      </Button>
                    </Stack>
                  </TableCell>
                </TableRow>
              ))
            )}
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
          Mostrando {filteredCustomers.length} de {customers.length} clientes
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Última actualización: {new Date().toLocaleTimeString()}
        </Typography>
      </Box>

      {/* Modal de registro */}
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ 
          background: "linear-gradient(135deg, #6c63ff 0%, #9d4edd 100%)", 
          color: 'white',
          display: 'flex',
          alignItems: 'center',
          gap: 1
        }}>
          <PersonAddIcon /> Nuevo Cliente
        </DialogTitle>
        
        <DialogContent sx={{ mt: 2 }}>
          <Stack spacing={3}>
            <TextField
              label="Nombre Completo"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              fullWidth
              required
              helperText="Nombre y apellido del cliente"
            />
            
            <TextField
              label="RUT"
              value={form.rut}
              onChange={(e) => setForm({ ...form, rut: e.target.value })}
              fullWidth
              required
              helperText="Formato: 12345678-9"
            />
            
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <TextField
                  label="Teléfono"
                  value={form.phone}
                  onChange={(e) => setForm({ ...form, phone: e.target.value })}
                  fullWidth
                  required
                  helperText="+56 9 1234 5678"
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="Email"
                  type="email"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                  fullWidth
                  required
                  helperText="correo@ejemplo.com"
                />
              </Grid>
            </Grid>
          </Stack>
        </DialogContent>
        
        <DialogActions>
          <Button onClick={() => setOpen(false)} sx={{ color: "#666" }}>
            Cancelar
          </Button>
          <Button 
            onClick={handleRegister} 
            variant="contained"
            disabled={!form.name || !form.rut || !form.phone || !form.email}
            sx={{ 
              background: "#6c63ff",
              "&:hover": { background: "#5a52d5" }
            }}
          >
            Registrar Cliente
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
};

export default CustomerView;