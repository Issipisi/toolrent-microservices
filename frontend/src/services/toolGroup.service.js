// src/services/toolGroup.service.js
import api from "../http-common";

const getAll = () => {
  return api.get('/api/tools/groups');
};

const getToolGroup = (id) => {
  return api.get(`/api/tools/groups/${id}`);
};

const createToolGroup = (toolGroupData, userName) => {
  return api.post('/api/tools/groups', toolGroupData, {
    params: { userName } // ← ambos
  });
};


const updateTariff = (groupId, dailyRentalRate, dailyFineRate) => {
  return api.put(`/api/tools/groups/${groupId}/tariff`, null, {
    params: { dailyRentalRate, dailyFineRate }
  });
};

const updateReplacementValue = (groupId, replacementValue) => {
  return api.put(`/api/tools/groups/${groupId}/replacement-value`, null, {
    params: { replacementValue }
  });
};

const getAvailableToolGroups = () => {
  return api.get('/api/tools/groups/available');
};

// Reparaciones (endpoints en ToolGroupController)
const sendToRepair = (unitId, reason, customerId) => {
  return api.put(`/api/tools/groups/units/${unitId}/send-to-repair`, null, {
    params: { reason, customerId }
  });
};

const completeRepair = (unitId, repairCost, successful, notes) => {
  return api.put(`/api/tools/groups/units/${unitId}/complete-repair`, null, {
    params: { repairCost, successful, notes }
  });
};

const retireTool = (unitId, reason, customerId) => {
  return api.put(`/api/tools/groups/units/${unitId}/retire`, null, {
    params: { reason, customerId }
  });
};

export default { getAll, getToolGroup, createToolGroup, updateTariff, updateReplacementValue, getAvailableToolGroups,
  sendToRepair, completeRepair, retireTool
};