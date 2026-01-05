import api from "../http-common";

const getAll = () => api.get('/api/customers');
const getCustomer = (id) => api.get(`/api/customers/${id}`);
const register = (customerData) => api.post('/api/customers', customerData);
const updateStatus = (id, status) => api.put(`/api/customers/${id}/status`, null, {
  params: { status }
});
const getActive = () => api.get('/api/customers/active');
const validateForLoan = (customerId) => api.get(`/api/customers/${customerId}/validate-loan`);
const getActiveLoansCount = (customerId) => api.get(`/api/customers/${customerId}/active-count`);

export default { 
  getAll, 
  getCustomer, 
  register, 
  updateStatus, 
  getActive, 
  validateForLoan, 
  getActiveLoansCount 
};