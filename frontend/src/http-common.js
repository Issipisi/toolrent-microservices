// src/http-common.js
import axios from "axios";
import keycloak from "./services/keycloak";

// Gateway es el punto de entrada único
const API_GATEWAY_URL = "http://localhost:8080"; // Gateway en puerto 8080

console.log("API Gateway URL:", API_GATEWAY_URL);

const api = axios.create({
  baseURL: API_GATEWAY_URL,
  headers: {
    "Content-Type": "application/json"
  }
});

api.interceptors.request.use(async (config) => {
  if (keycloak.authenticated) {
    try {
      // Actualizar token si está por expirar (30 segundos de margen)
      const minValidity = 30;
      await keycloak.updateToken(minValidity);
      config.headers.Authorization = `Bearer ${keycloak.token}`;
    } catch (error) {
      console.error("Error actualizando token:", error);
      keycloak.login();
      return Promise.reject(error);
    }
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

// Interceptor de respuesta para manejar errores
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      console.log("Token expirado o inválido, redirigiendo a login");
      keycloak.login();
    } else if (error.response && error.response.status === 403) {
      console.log("Acceso denegado - Permisos insuficientes");
      alert("No tienes permisos para realizar esta acción");
    }
    return Promise.reject(error);
  }
);

export default api;