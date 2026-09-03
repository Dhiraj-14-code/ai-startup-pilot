import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import ProjectDetails from './pages/ProjectDetails';

const PrivateRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  return token ? children : <Navigate to="/login" />;
};

function App() {
  return (
    <Router>
      <div className="app-container">
        <header className="app-header">
          <h1>StartupPilot AI</h1>
          {localStorage.getItem('token') && (
            <button onClick={() => { localStorage.removeItem('token'); window.location.reload(); }}>
              Logout
            </button>
          )}
        </header>
        <main className="app-main">
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
            <Route path="/projects/:id" element={<PrivateRoute><ProjectDetails /></PrivateRoute>} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
