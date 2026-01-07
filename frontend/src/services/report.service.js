// src/services/report.service.js
import api from "../http-common";

const getActiveLoansReport = () => {
  return api.get('/api/reports/active-loans');
};

const getCustomersWithDelaysReport = () => {
  return api.get('/api/reports/customers-with-delays');
};


const getToolRanking = () => {
  return api.get('/api/reports/tool-ranking');
};

export default { 
  getActiveLoansReport, 
  getCustomersWithDelaysReport,
  getToolRanking 
};