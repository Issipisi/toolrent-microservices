// src/services/report.service.js
import api from "../http-common";

const getActiveLoansReport = () => {
  return api.get('/api/reports/active-loans');
};

const getCustomersWithDelaysReport = () => {
  return api.get('/api/reports/customers-with-delays');
};

const getMostBorrowedToolsReport = (startDate, endDate) => {
  return api.get('/api/reports/most-borrowed-tools', {
    params: { startDate, endDate }
  });
};


const getToolRanking = () => {
  return api.get('/api/reports/tool-ranking');
};

export default { 
  getActiveLoansReport, 
  getCustomersWithDelaysReport, 
  getMostBorrowedToolsReport,
  getToolRanking 
};