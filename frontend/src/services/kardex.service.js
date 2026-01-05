// src/services/kardex.service.js
import api from "../http-common";

const getAllMovements = () => {
  return api.get('/api/kardex/movements');
};

const getMovementsByToolUnit = (toolUnitId) => {
  return api.get(`/api/kardex/movements/tool-unit/${toolUnitId}`);
};

const getMovementsByToolGroup = (toolGroupId) => {
  return api.get(`/api/kardex/movements/tool-group/${toolGroupId}`);
};

const getMovementsByDateRange = (from, to) => {
  return api.get('/api/kardex/movements/date-range', {
    params: { from, to }
  });
};

const getMovementsByType = (movementType) => {
  return api.get(`/api/kardex/movements/type/${movementType}`);
};

const getMovementsByCustomer = (customerId) => {
  return api.get(`/api/kardex/movements/customer/${customerId}`);
};

// Registrar movimientos específicos
const registerToolCreation = (toolUnitId, toolGroupId, toolName) => {
  return api.post('/api/kardex/movements/tool-creation', null, {
    params: { toolUnitId, toolGroupId, toolName }
  });
};

const registerLoan = (toolUnitId, toolGroupId, customerId, customerName) => {
  return api.post('/api/kardex/movements/loan', null, {
    params: { toolUnitId, toolGroupId, customerId, customerName }
  });
};

const registerReturn = (toolUnitId, toolGroupId, customerId, customerName, damaged, overdue) => {
  return api.post('/api/kardex/movements/return', null, {
    params: { toolUnitId, toolGroupId, customerId, customerName, damaged, overdue }
  });
};

const registerSendToRepair = (toolUnitId, toolGroupId, customerId, reason) => {
  return api.post('/api/kardex/movements/send-to-repair', null, {
    params: { toolUnitId, toolGroupId, customerId, reason }
  });
};

const registerReEntry = (toolUnitId, toolGroupId, repairCost, notes) => {
  return api.post('/api/kardex/movements/re-entry', null, {
    params: { toolUnitId, toolGroupId, repairCost, notes }
  });
};

const registerRetirement = (toolUnitId, toolGroupId, reason, customerId) => {
  return api.post('/api/kardex/movements/retirement', null, {
    params: { toolUnitId, toolGroupId, reason, customerId }
  });
};

export default { getAllMovements, getMovementsByToolUnit, getMovementsByToolGroup, getMovementsByDateRange, getMovementsByType,
  getMovementsByCustomer, registerToolCreation, registerLoan, registerReturn, registerSendToRepair, registerReEntry, registerRetirement
};