import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";
import IconButton from "@mui/material/IconButton";
import MenuIcon from "@mui/icons-material/Menu";
import Sidemenu from "./Sidemenu";
import { useState } from "react";
import { useKeycloak } from "@react-keycloak/web"; 
import { Avatar, Chip, Badge } from "@mui/material";
import LogoutIcon from "@mui/icons-material/Logout";
import PersonIcon from "@mui/icons-material/Person";
import { useNavigate } from "react-router-dom";

export default function Navbar({ userRole }) {
  const [open, setOpen] = useState(false);
  const { keycloak, initialized } = useKeycloak();
  const navigate = useNavigate();

  const toggleDrawer = (open) => (event) => {
    if (event.type === 'keydown' && (event.key === 'Tab' || event.key === 'Shift')) {
      return;
    }
    setOpen(open);
  };

  // Obtener información del usuario
  const getUserName = () => {
    return keycloak.tokenParsed?.preferred_username ||
           keycloak.tokenParsed?.email ||
           "Usuario";
  };

  const getUserInitials = () => {
    const name = getUserName();
    return name.charAt(0).toUpperCase();
  };

  const getRoleColor = () => {
    return userRole === 'ADMIN' ? 'error' : 'success';
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar 
        position="static" 
        sx={{ 
          background: "linear-gradient(135deg, #6c63ff 0%, #9d4edd 100%)",
          boxShadow: '0 4px 20px rgba(108, 99, 255, 0.3)'
        }}
      >
        <Toolbar>
          {/* Botón del menú */}
          <IconButton
            size="large"
            edge="start"
            color="inherit"
            aria-label="menu"
            sx={{ 
              mr: 2,
              '&:hover': { 
                backgroundColor: 'rgba(255,255,255,0.1)'
              }
            }}
            onClick={toggleDrawer(true)}
          >
            <MenuIcon />
          </IconButton>

          {/* Título principal */}
          <Box sx={{ 
            display: 'flex', 
            alignItems: 'center', 
            gap: 1, 
            flexGrow: 1 
          }}>

            <Typography 
              variant="h6" 
              component="div" 
              sx={{ 
                fontWeight: 'bold',
                fontSize: { xs: '1.1rem', sm: '1.25rem' },
                cursor: 'pointer',
                transition: 'all 0.2s ease',
                '&:hover': {
                  opacity: 0.85,
                  transform: 'scale(1.1)'
                },
                '&:active': {
                  transform: 'translateY(1px)' // 👈 Efecto de presionado
                }
              }}
              onClick={() => navigate("/home")}
            >
              ToolRent
            </Typography>
            
            <Typography 
              variant="subtitle2" 
              sx={{ 
                opacity: 0.9,
                fontSize: '0.8rem',
                display: { xs: 'none', sm: 'block' }
              }}
            >
              Sistema de Gestión
            </Typography>

            {/* Indicador de rol */}
            {userRole && (
              <Chip
                label={userRole}
                size="small"
                color={getRoleColor()}
                sx={{ 
                  ml: 1,
                  fontWeight: 'bold',
                  fontSize: '0.7rem',
                  height: 22
                }}
              />
            )}
          </Box>

          {/* Información del usuario */}
          {initialized && keycloak.authenticated && (
            <Box sx={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: 2 
            }}>
              

              {/* Avatar del usuario */}
              <Avatar
                sx={{
                  width: 36,
                  height: 36,
                  bgcolor: 'rgba(255,255,255,0.2)',
                  color: 'white',
                  fontWeight: 'bold',
                  border: '2px solid rgba(255,255,255,0.3)'
                }}
              >
                {getUserInitials()}
              </Avatar>

              {/* Nombre de usuario (solo en desktop) */}
              <Typography 
                sx={{ 
                  mr: 1,
                  display: { xs: 'none', md: 'block' },
                  fontSize: '0.9rem',
                  fontWeight: 'medium'
                }}
              >
                {getUserName()}
              </Typography>

              {/* Botón de logout */}
              <Button
                color="inherit"
                startIcon={<LogoutIcon />}
                onClick={() => keycloak.logout()}
                sx={{
                  textTransform: 'none',
                  fontWeight: 'medium',
                  fontSize: '0.9rem',
                  '&:hover': {
                    backgroundColor: 'rgba(255,255,255,0.1)'
                  }
                }}
              >
                <span style={{ display: { xs: 'none', sm: 'inline' } }}>
                  Salir
                </span>
              </Button>
            </Box>
          )}
        </Toolbar>
      </AppBar>

      {/* Sidemenu (se mantiene igual) */}
      <Sidemenu open={open} toggleDrawer={toggleDrawer} userRole={userRole} />
    </Box>
  );
}