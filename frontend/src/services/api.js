import axios from 'axios';

const BACKEND_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
const AI_URL = import.meta.env.VITE_AI_API_BASE_URL || 'http://localhost:8000/api/v1';

const api = axios.create({
  baseURL: BACKEND_URL,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// ── Backend API ──────────────────────────────────────────────
export const login = (email, password) => api.post('/auth/login', { email, password });
export const register = (userData) => api.post('/users/register', userData);
export const getProjects = () => api.get('/projects');
export const getProject = (id) => api.get(`/projects/${id}`);
export const getProjectHealth = (id) => api.get(`/projects/${id}/health`);

// ── AI Service: File Upload ──────────────────────────────────
export const uploadFileForAnalysis = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return axios.post(`${AI_URL}/analyze/file`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

// ── AI Service: Startup Analytics ───────────────────────────
export const analyzeStartupData = (businessData) =>
  axios.post(`${AI_URL}/analyze/startup`, businessData, {
    headers: { 'Content-Type': 'application/json' },
  });

export const analyzeStartupFile = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return axios.post(`${AI_URL}/analyze/file`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export default api;
