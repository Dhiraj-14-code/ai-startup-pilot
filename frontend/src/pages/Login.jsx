import React, { useState } from 'react';
import { login } from '../api';
import { useNavigate, Link } from 'react-router-dom';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await login(email, password);
      localStorage.setItem('token', res.data.token);
      navigate('/');
    } catch (err) {
      setError('Invalid credentials');
    }
  };

  return (
    <div className="login-container">
      <form onSubmit={handleLogin} className="login-form">
        <h2>Login to StartupPilot</h2>
        {error && <p className="error">{error}</p>}
        <div className="form-group">
          <label>Email</label>
          <input type="email" value={email} onChange={e => setEmail(e.target.value)} required />
        </div>
        <div className="form-group">
          <label>Password</label>
          <input type="password" value={password} onChange={e => setPassword(e.target.value)} required />
        </div>
        <button type="submit">Login</button>
        <p style={{ marginTop: '1rem', textAlign: 'center' }}>
          Don't have an account? <Link to="/register">Register</Link>
        </p>
      </form>
    </div>
  );
};

export default Login;
