import api from "../http-common";

const register = (loanData, userName) => {
  return api.post('/api/loans', loanData, {
    params: { userName } // ← ahora sí llega al backend
  });
};

const returnLoan = (loanId, damageCharge = 0.0, irreparable = false, userName = "Sistema") => {
  return api.put(`/api/loans/${loanId}/return`, null, {
    params: { damageCharge, irreparable, userName } 
  });
};

const getActiveLoans = () => {
  return api.get('/api/loans/active');
};

const getOverdueLoans = () => {
  return api.get('/api/loans/overdue');
};

const getReturnedWithDebts = () => {
  return api.get('/api/loans/returned-with-debts');
};

const payDebts = (loanId) => {
  return api.put(`/api/loans/${loanId}/pay-debts`);
};

const applyDamage = (loanId, amount, irreparable = false) => {
  return api.put(`/api/loans/${loanId}/damage`, null, {
    params: { amount, irreparable }
  });
};

const getActive = () => {
  return api.get('/api/loans/active');
};

const sendToRepair = (toolUnitId) => {
  return api.put(`/api/tools/${toolUnitId}/status`, {
    status: "IN_REPAIR"
  });
};

export default { 
  register, 
  returnLoan, 
  getActive,
  getActiveLoans, 
  getOverdueLoans, 
  getReturnedWithDebts, 
  payDebts, 
  applyDamage,
  sendToRepair  // <-- AÑADIR ESTO
};