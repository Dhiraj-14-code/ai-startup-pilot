import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, NavLink } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import ProjectDetails from './pages/ProjectDetails';
import StartupAnalytics from './pages/StartupAnalytics';

const PrivateRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  return token ? children : <Navigate to="/login" />;
};

function App() {
  const token = localStorage.getItem('token');
  const logout = () => { localStorage.removeItem('token'); window.location.reload(); };

  return (
    <Router>
      <div className="app-container">
        <header className="app-header">
          <h1>🚀 StartupPilot AI</h1>
          <nav className="nav-links">
            {token && <>
              <NavLink to="/" end className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
                Dashboard
              </NavLink>
              <NavLink to="/analyze" className={({ isActive }) => `nav-link cta${isActive ? ' active' : ''}`}>
                ✨ AI Analytics
              </NavLink>
              <button className="logout-btn" onClick={logout}>Logout</button>
            </>}
          </nav>
        </header>
        <main className="app-main">
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
            <Route path="/projects/:id" element={<PrivateRoute><ProjectDetails /></PrivateRoute>} />
            <Route path="/analyze" element={<PrivateRoute><StartupAnalytics /></PrivateRoute>} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
