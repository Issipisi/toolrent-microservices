import api from "../http-common";

const getAll = () => {
  return api.get('/api/tools/units');
};

const getAllWithDetails = () => {
  return api.get('/api/tools/units/details');
};

const getToolUnit = (id) => {
  return api.get(`/api/tools/units/${id}`);
};

const getUnitModel = (id) => {
  return api.get(`/api/tools/units/${id}/model`);
};

const getAvailableUnit = (groupId) => {
  return api.get(`/api/tools/units/groups/${groupId}/available`);
};

const updateStatus = (id, status, userName) => {
  return api.put(`/api/tools/units/${id}/status`, null, {
    params: { status, userName } // ← ahora sí lo envía
  });
};

const changeStatus = (id, status, userName) => {
  return updateStatus(id, status, userName);
};

const getAvailableStock = (groupId) => {
  return api.get(`/api/tools/units/groups/${groupId}/stock`);
};

const repairResolution = (id, retire, userName) =>
  api.put(`/api/tools/units/${id}/repair-resolution`, null, { params: { retire, userName } });

export default {
  getAll,
  getAllWithDetails,
  getToolUnit,
  getUnitModel,
  getAvailableUnit,
  updateStatus,
  changeStatus,
  getAvailableStock,
  repairResolution
};