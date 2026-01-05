// src/App.jsx - VERSIÓN COMPLETA CON ROLES
import './App.css'
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom'
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
import { useKeycloak } from "@react-keycloak/web";  // ✅ Añadir este import

function App() {
  const { keycloak, initialized } = useKeycloak();  // ✅ Obtener Keycloak

  // Mostrar loading mientras Keycloak se inicializa
  if (!initialized) {
    return <div style={{ padding: '20px' }}>Inicializando autenticación...</div>;
  }

  // Si no está autenticado, Keycloak se encargará (onLoad: 'login-required')
  // pero podemos agregar un check adicional
  if (!keycloak.authenticated) {
    return <div style={{ padding: '20px' }}>Redirigiendo a login...</div>;
  }

  // ---- OBTENER ROL DEL USUARIO ----
  // Opción 1: Roles desde realm_access (recomendado)
  const roles = keycloak.tokenParsed?.realm_access?.roles || [];
  
  // Opción 2: Roles desde resource_access (si los configuraste en el cliente)
  // const clientRoles = keycloak.tokenParsed?.resource_access?.['toolrent-frontend']?.roles || [];
  // const roles = [...realmRoles, ...clientRoles];
  
  const userRole = roles.includes("ADMIN") ? "ADMIN" : 
                   roles.includes("EMPLOYEE") ? "EMPLOYEE" : 
                   null;

  console.log('Usuario autenticado:', keycloak.tokenParsed?.preferred_username);
  console.log('Roles disponibles:', roles);
  console.log('Rol asignado:', userRole);

  // Componente para proteger rutas
  const PrivateRoute = ({ element, rolesAllowed }) => {
    if (!userRole || !rolesAllowed.includes(userRole)) {
      return (
        <div style={{ padding: '20px', textAlign: 'center' }}>
          <h3>⚠️ Acceso Denegado</h3>
          <p>No tienes permisos para acceder a esta página.</p>
          <p>Tu rol: {userRole || 'No asignado'}</p>
          <p>Roles requeridos: {rolesAllowed.join(', ')}</p>
        </div>
      );
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