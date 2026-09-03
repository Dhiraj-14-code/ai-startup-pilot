import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const login = (email, password) => api.post('/auth/login', { email, password });
export const register = (userData) => api.post('/users/register', userData);
export const getProjects = () => api.get('/projects');
export const getProject = (id) => api.get(`/projects/${id}`);
export const getProjectHealth = (id) => api.get(`/projects/${id}/health`);
export const uploadFileForAnalysis = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return axios.post(import.meta.env.VITE_AI_API_BASE_URL || 'http://localhost:8000/api/v1/analyze/file', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    }
  });
};

export default api;
