import api from "../http-common";

const register = (loanData) => {
  return api.post('/api/loans', loanData);
};

const returnLoan = (loanId, damageCharge = 0.0, irreparable = false) => {
  return api.put(`/api/loans/${loanId}/return`, null, {
    params: { damageCharge, irreparable }
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

export default { 
  register, 
  returnLoan, 
  getActive,
  getActiveLoans, 
  getOverdueLoans, 
  getReturnedWithDebts, 
  payDebts, 
  applyDamage 
};