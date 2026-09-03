import React, { useState } from 'react';
import { register } from '../api';
import { useNavigate, Link } from 'react-router-dom';

const Register = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      await register({ name, email, password });
      setSuccess(true);
      setTimeout(() => navigate('/login'), 1500);
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    }
  };

  return (
    <div className="login-container">
      <form onSubmit={handleRegister} className="login-form">
        <h2>Register for StartupPilot</h2>
        {error && <p className="error">{error}</p>}
        {success && <p className="success">Registration successful! Redirecting to login...</p>}
        <div className="form-group">
          <label>Name</label>
          <input type="text" value={name} onChange={e => setName(e.target.value)} required />
        </div>
        <div className="form-group">
          <label>Email</label>
          <input type="email" value={email} onChange={e => setEmail(e.target.value)} required />
        </div>
        <div className="form-group">
          <label>Password (min 8 characters)</label>
          <input type="password" value={password} onChange={e => setPassword(e.target.value)} required minLength={8} />
        </div>
        <button type="submit">Register</button>
        <p style={{ marginTop: '1rem', textAlign: 'center' }}>
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </form>
    </div>
  );
};

export default Register;
