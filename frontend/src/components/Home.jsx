import { Box, Typography, Paper, Grid, Card, CardContent, Button, Stack, Divider } from "@mui/material";
import { Link } from "react-router-dom";
import BuildIcon from '@mui/icons-material/Build';
import PeopleIcon from '@mui/icons-material/People';
import LocalActivityIcon from '@mui/icons-material/LocalActivity';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import AnalyticsIcon from '@mui/icons-material/Analytics';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import HomeIcon from '@mui/icons-material/Home';
import SecurityIcon from '@mui/icons-material/Security';

const Home = () => {
  const features = [
    {
      title: "Gestión de Inventario",
      description: "Control completo de herramientas disponibles, estados y stock.",
      icon: <BuildIcon fontSize="large" />,
      path: "/tools",
      color: "#6c63ff"
    },
    {
      title: "Préstamos y Devoluciones",
      description: "Automatiza el ciclo completo de préstamos con cálculo de multas.",
      icon: <LocalActivityIcon fontSize="large" />,
      path: "/loans",
      color: "#2196f3"
    },
    {
      title: "Gestión de Clientes",
      description: "Administra información de clientes y restricciones por deudas.",
      icon: <PeopleIcon fontSize="large" />,
      path: "/customers",
      color: "#4caf50"
    },
    {
      title: "Configuración de Tarifas",
      description: "Define tarifas de alquiler, multas y valores de reposición.",
      icon: <AttachMoneyIcon fontSize="large" />,
      path: "/tariff",
      color: "#ff9800"
    },
    {
      title: "Kardex y Movimientos",
      description: "Registro histórico completo de todas las transacciones.",
      icon: <AnalyticsIcon fontSize="large" />,
      path: "/kardex",
      color: "#9c27b0"
    },
    {
      title: "Reportes y Estadísticas",
      description: "Genera reportes de préstamos, clientes y herramientas más solicitadas.",
      icon: <ReceiptLongIcon fontSize="large" />,
      path: "/reports",
      color: "#f44336"
    }
  ];

  return (
    <Paper sx={{ p: 4, background: "#ffffff", minHeight: '80vh' }}>
      {/* Encabezado */}
      <Box sx={{ textAlign: 'center', mb: 6 }}>
        <Typography variant="h3" sx={{ 
          color: "#6c63ff", 
          mb: 2,
          fontWeight: 'bold',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 2
        }}>
          <HomeIcon fontSize="large" />
          ToolRent - Sistema de Gestión
        </Typography>
        
        <Typography variant="h6" sx={{ color: "#666", mb: 3, maxWidth: '800px', mx: 'auto' }}>
          Automatiza el alquiler de herramientas con control digital en tiempo real.
          Reemplaza registros manuales por un sistema eficiente y transparente.
        </Typography>
        
        <Stack direction="row" spacing={2} justifyContent="center" sx={{ mb: 6 }}>
          <Button 
            variant="contained" 
            size="large"
            component={Link}
            to="/loans"
            sx={{ 
              background: "linear-gradient(135deg, #6c63ff 0%, #9d4edd 100%)",
              px: 4
            }}
          >
            Comenzar Préstamo
          </Button>
          <Button 
            variant="outlined" 
            size="large"
            component={Link}
            to="/tools"
          >
            Ver Inventario
          </Button>
        </Stack>
      </Box>

      
  

      {/* Tecnologías */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {/* Información técnica al final */}
        <Card sx={{ backgroundColor: '#f0f4ff', mb: 4 }}>
          <CardContent>
            <Typography variant="h5" sx={{ mb: 2, color: "#6c63ff", display: 'flex', alignItems: 'center', gap: 1 }}>
              <SecurityIcon /> Tecnologías Utilizadas
            </Typography>
            
            <Grid container spacing={2}>
              <Grid item xs={12} md={6}>
                <Typography variant="h6" sx={{ mb: 1 }}>Backend</Typography>
                <Stack spacing={0.5}>
                  <Typography variant="body2">• Spring Boot 3.4.5 - Framework Java</Typography>
                  <Typography variant="body2">• Microservicios independientes</Typography>
                  <Typography variant="body2">• MySQL - Base de datos relacional</Typography>
                  <Typography variant="body2">• Eureka - Service Discovery</Typography>
                  <Typography variant="body2">• API Gateway - Enrutamiento centralizado</Typography>
                </Stack>
              </Grid>
              
              <Grid item xs={12} md={6}>
                <Typography variant="h6" sx={{ mb: 1 }}>Frontend</Typography>
                <Stack spacing={0.5}>
                  <Typography variant="body2">• React 18 - Biblioteca JavaScript</Typography>
                  <Typography variant="body2">• Material-UI - Componentes UI</Typography>
                  <Typography variant="body2">• Keycloak - Autenticación y Roles</Typography>
                  <Typography variant="body2">• Axios - Cliente HTTP</Typography>
                  <Typography variant="body2">• Day.js - Manejo de fechas</Typography>
                </Stack>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      </Grid>

      {/* Pie */}
      <Divider sx={{ my: 4 }} />
      
      <Box sx={{ textAlign: 'center' }}>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
          ToolRent v1.0.0 | Sistema desarrollado para la gestión de alquiler de herramientas
        </Typography>
        <Typography variant="caption" color="text.secondary">
          © {new Date().getFullYear()} - Todos los derechos reservados
        </Typography>
      </Box>
    </Paper>
  );
};

export default Home;