import { useEffect, useState } from "react";
import keycloak from "../services/keycloak";
import toolGroupService from "../services/toolGroup.service";
import { 
  Button, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  TextField, Typography, Stack, Dialog, DialogTitle, DialogContent, DialogActions,
  Box, Chip, Alert, Grid, Card, CardContent,
  MenuItem  // <-- AÑADIR ESTE IMPORT
} from "@mui/material";
import AddIcon from '@mui/icons-material/Add';
import BuildIcon from '@mui/icons-material/Build';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';

const ToolGroupView = () => {
  const [groups, setGroups] = useState([]);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [form, setForm] = useState({ 
    name: "", 
    category: "", 
    replacementValue: "", 
    pricePerDay: "", 
    stock: "",
    dailyFineRate: "3000"
  });

  const loadGroups = async () => {
    setLoading(true);
    try {
      const res = await toolGroupService.getAll();
      setGroups(res.data || []);
    } catch (error) {
      setMessage({ type: 'error', text: 'Error cargando grupos' });
      console.error("Error:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async () => {
    try {
      const userName = keycloak.tokenParsed?.preferred_username || keycloak.tokenParsed?.email || "Usuario";

      const toolGroupData = {
        name: form.name,
        category: form.category,
        replacementValue: parseFloat(form.replacementValue),
        initialStock: parseInt(form.stock),
        dailyRentalRate: parseFloat(form.pricePerDay),
        dailyFineRate: parseFloat(form.dailyFineRate)
      };

      await toolGroupService.createToolGroup(toolGroupData, userName);
      
      setMessage({ type: 'success', text: 'Grupo creado exitosamente' });
      setOpen(false);
      setForm({ 
        name: "", category: "", replacementValue: "", 
        pricePerDay: "", stock: "", dailyFineRate: "3000" 
      });
      loadGroups();
      
      setTimeout(() => setMessage({ type: '', text: '' }), 3000);
    } catch (e) {
      setMessage({ type: 'error', text: e.response?.data?.message || "Error al crear grupo" });
    }
  };

  const stats = {
    totalGroups: groups.length,
    totalStock: groups.reduce((sum, g) => sum + (g.totalStock || 0), 0),
    availableStock: groups.reduce((sum, g) => sum + (g.availableCount || 0), 0),
    categories: [...new Set(groups.map(g => g.category))].length
  };

  useEffect(() => { 
    loadGroups(); 
  }, []);

  return (
    <Paper sx={{ p: 4, background: "#ffffff" }}>
      {/* Encabezado */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" sx={{ color: "#6c63ff", display: 'flex', alignItems: 'center', gap: 1 }}>
            <BuildIcon /> Grupos de Herramientas
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Administra los grupos y categorías de herramientas
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
          Nuevo Grupo
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
              <Typography variant="h4" color="primary">
                {stats.totalGroups}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Grupos Totales
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} sm={3}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" color="success.main">
                {stats.availableStock}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Disponibles
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} sm={3}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" color="info.main">
                {stats.categories}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Categorías
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

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
              <TableCell>Nombre</TableCell>
              <TableCell>Categoría</TableCell>
              <TableCell>Tarifa Diaria</TableCell>
              <TableCell>Multa Diaria</TableCell>
              <TableCell>Valor Reposición</TableCell>
              <TableCell>Stock</TableCell>
              <TableCell>Estado</TableCell>
            </TableRow>
          </TableHead>
          
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 3 }}>
                  Cargando grupos...
                </TableCell>
              </TableRow>
            ) : groups.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 3 }}>
                  No hay grupos registrados
                </TableCell>
              </TableRow>
            ) : (
              groups.map((g) => (
                <TableRow 
                  key={g.id} 
                  hover 
                  sx={{ 
                    '&:hover': { backgroundColor: '#f5f0ff' },
                    '&:nth-of-type(even)': { backgroundColor: '#f9f9f9' }
                  }}
                >
                  <TableCell sx={{ fontWeight: 'medium' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <BuildIcon fontSize="small" color="action" />
                      {g.name}
                    </Box>
                  </TableCell>
                  
                  <TableCell>
                    <Chip 
                      label={g.category} 
                      size="small"
                      variant="outlined"
                      sx={{ 
                        backgroundColor: g.category === 'Manual' ? '#e3f2fd' : 
                                        g.category === 'Eléctrica' ? '#f3e5f5' : 
                                        g.category === 'Neumática' ? '#e8f5e9' : '#fff3e0'
                      }}
                    />
                  </TableCell>
                  
                  <TableCell sx={{ fontFamily: 'monospace' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <AttachMoneyIcon fontSize="small" color="action" />
                      {g.dailyRentalRate?.toLocaleString() || '0'}
                    </Box>
                  </TableCell>
                  
                  <TableCell sx={{ fontFamily: 'monospace' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <AttachMoneyIcon fontSize="small" color="error" />
                      {g.dailyFineRate?.toLocaleString() || '3,000'}
                    </Box>
                  </TableCell>
                  
                  <TableCell sx={{ fontFamily: 'monospace' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <AttachMoneyIcon fontSize="small" color="success" />
                      {g.replacementValue?.toLocaleString() || '0'}
                    </Box>
                  </TableCell>
                  
                  <TableCell>
                    <Box sx={{ 
                      backgroundColor: g.availableCount > 0 ? '#e8f5e9' : '#ffebee', 
                      px: 2, 
                      py: 0.5, 
                      borderRadius: 1,
                      display: 'inline-block',
                      fontSize: '0.85rem'
                    }}>
                      {g.availableCount || 0}
                    </Box>
                  </TableCell>
                  
                  <TableCell>
                    <Chip 
                      label={g.availableCount > 0 ? "DISPONIBLE" : "AGOTADO"} 
                      size="small"
                      color={g.availableCount > 0 ? "success" : "error"}
                    />
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
          Mostrando {groups.length} grupos
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
          <AddIcon /> Nuevo Grupo de Herramientas
        </DialogTitle>
        
        <DialogContent sx={{ mt: 2 }}>
          <Stack spacing={3}>
            <TextField
              label="Nombre del Grupo"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              fullWidth
              required
              helperText="Ej: Martillo Pesado, Taladro Percutor"
            />
            
            <TextField
              select
              label="Categoría"
              value={form.category}
              onChange={(e) => setForm({ ...form, category: e.target.value })}
              fullWidth
              required
            >
              <MenuItem value="Manual">Manual</MenuItem>
              <MenuItem value="Eléctrica">Eléctrica</MenuItem>
              <MenuItem value="Neumática">Neumática</MenuItem>
              <MenuItem value="Hidráulica">Hidráulica</MenuItem>
              <MenuItem value="Corte">Corte</MenuItem>
              <MenuItem value="Sujeción y Fijación">Sujeción y Fijación</MenuItem>
              <MenuItem value="Atornillado y Llaves">Atornillado y Llaves</MenuItem>
              <MenuItem value="Alicates y Pinzas">Alicates y Pinzas</MenuItem>
              <MenuItem value="Lijado y Acabado">Lijado y Acabado</MenuItem>
              <MenuItem value="Pintura y Decoración">Pintura y Decoración</MenuItem>
              <MenuItem value="Soldadura">Soldadura</MenuItem>
              <MenuItem value="Construcción y Albañilería">Construcción y Albañilería</MenuItem>
            </TextField>
            
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <TextField
                  label="Tarifa Diaria ($)"
                  type="number"
                  value={form.pricePerDay}
                  onChange={(e) => setForm({ ...form, pricePerDay: e.target.value })}
                  fullWidth
                  required
                  helperText="Precio por día de alquiler"
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="Multa Diaria ($)"
                  type="number"
                  value={form.dailyFineRate}
                  onChange={(e) => setForm({ ...form, dailyFineRate: e.target.value })}
                  fullWidth
                  required
                  helperText="Multa por atraso por día"
                />
              </Grid>
            </Grid>
            
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <TextField
                  label="Valor de Reposición ($)"
                  type="number"
                  value={form.replacementValue}
                  onChange={(e) => setForm({ ...form, replacementValue: e.target.value })}
                  fullWidth
                  required
                  helperText="Costo para reemplazar"
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="Stock Inicial"
                  type="number"
                  value={form.stock}
                  onChange={(e) => setForm({ ...form, stock: e.target.value })}
                  fullWidth
                  required
                  helperText="Cantidad inicial disponible"
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
            disabled={!form.name || !form.category || !form.pricePerDay || !form.stock}
            sx={{ 
              background: "#6c63ff",
              "&:hover": { background: "#5a52d5" }
            }}
          >
            Crear Grupo
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
};

export default ToolGroupView;