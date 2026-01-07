// src/App.jsx - VERSIÓN COMPLETA CON ROLES Y COMPONENTE ACCESSDENIED
import './App.css'
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom'
import { Box, CircularProgress, Typography } from '@mui/material'; // Añade estas importaciones
import Navbar from "./components/Navbar"
import Home from './components/Home';
import ToolUnitView from './components/ToolUnitView';
import ToolGroupView from './components/ToolGroupView';
import NotFound from './components/NotFound';
import CustomerView from "./components/CustomerView";
import LoanView from "./components/LoanView";
import TariffView from "./components/TariffView";
import KardexView from "./components/KardexView";
import ReportView from "./components/ReportView";
import AccessDenied from "./components/AccessDenied"; // NUEVO COMPONENTE
import { useKeycloak } from "@react-keycloak/web";  

function App() {
  const { keycloak, initialized } = useKeycloak(); 

  // Mostrar loading mientras Keycloak se inicializa
  if (!initialized) {
    return (
      <Box sx={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh' 
      }}>
        <CircularProgress />
      </Box>
    );
  }

  // Si no está autenticado, Keycloak se encargará (onLoad: 'login-required')
  if (!keycloak.authenticated) {
    return (
      <Box sx={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        flexDirection: 'column',
        gap: 2
      }}>
        <CircularProgress />
        <Typography>Redirigiendo al login...</Typography>
      </Box>
    );
  }

  // OBTENER ROL DEL USUARIO
  const roles = keycloak.tokenParsed?.realm_access?.roles || [];
  const userRole = roles.includes("ADMIN") ? "ADMIN" : 
                   roles.includes("EMPLOYEE") ? "EMPLOYEE" : 
                   null;

  console.log('Usuario autenticado:', keycloak.tokenParsed?.preferred_username);
  console.log('Roles disponibles:', roles);
  console.log('Rol asignado:', userRole);

  // Componente para proteger rutas usando el nuevo componente AccessDenied
  const PrivateRoute = ({ element, rolesAllowed }) => {
    if (!userRole || !rolesAllowed.includes(userRole)) {
      return <AccessDenied userRole={userRole} requiredRoles={rolesAllowed} />;
    }
    return element;
  };

  return (
    <Router>
      <div className="container">
        {/* ✅ Pasar el rol a Navbar */}
        <Navbar userRole={userRole} />
        
        <Routes>
          <Route path="/" element={<Navigate to="/home" replace />} />
          <Route path="/home" element={<Home />} />

          {/* Solo ADMIN */}
          <Route
            path="/customers/*"
            element={<PrivateRoute element={<CustomerView />} rolesAllowed={["ADMIN"]} />}
          />

          <Route
            path="/tools"
            element={<PrivateRoute element={<ToolGroupView />} rolesAllowed={["ADMIN"]} />}
          />

          <Route 
            path="/tools/units" 
            element={<PrivateRoute element={<ToolUnitView />} rolesAllowed={["ADMIN"]} />} 
          />

          {/* ADMIN y EMPLOYEE */}
          <Route 
            path="/loans" 
            element={<PrivateRoute element={<LoanView />} rolesAllowed={["ADMIN", "EMPLOYEE"]} />} 
          />

          {/* Solo ADMIN */}
          <Route 
            path="/tariff" 
            element={<PrivateRoute element={<TariffView />} rolesAllowed={["ADMIN"]} />} 
          />

          <Route
            path="/kardex"
            element={<PrivateRoute element={<KardexView />} rolesAllowed={["ADMIN"]} />}
          />

          {/* ADMIN y EMPLOYEE */}
          <Route
            path="/reports"
            element={<PrivateRoute element={<ReportView />} rolesAllowed={["ADMIN", "EMPLOYEE"]} />}
          />

          <Route path="*" element={<NotFound />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;