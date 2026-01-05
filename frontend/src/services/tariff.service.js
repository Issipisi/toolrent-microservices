// src/services/tariff.service.js
import api from "../http-common";

const getAll = () => {
  return api.get('/api/tariffs');
};

const getTariff = (id) => {
  return api.get(`/api/tariffs/${id}`);
};

const createTariff = (dailyRentalRate, dailyFineRate) => {
  return api.post('/api/tariffs', null, {
    params: { dailyRentalRate, dailyFineRate }
  });
};

const updateTariff = (id, dailyRentalRate, dailyFineRate) => {
  return api.put(`/api/tariffs/${id}`, null, {
    params: { dailyRentalRate, dailyFineRate }
  });
};

export default { getAll, getTariff, createTariff, updateTariff };