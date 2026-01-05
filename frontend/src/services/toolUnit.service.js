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

const updateStatus = (id, status) => {
  return api.put(`/api/tools/units/${id}/status`, null, {
    params: { status }
  });
};

const changeStatus = (id, status) => {
  return updateStatus(id, status);
};

const getAvailableStock = (groupId) => {
  return api.get(`/api/tools/units/groups/${groupId}/stock`);
};

export default {
  getAll,
  getAllWithDetails,
  getToolUnit,
  getUnitModel,
  getAvailableUnit,
  updateStatus,
  changeStatus,
  getAvailableStock
};