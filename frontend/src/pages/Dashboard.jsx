import React, { useEffect, useState } from 'react';
import { getProjects, uploadFileForAnalysis } from '../services/api';
import { Link } from 'react-router-dom';
import AIInsights from '../components/AIInsights';

const Dashboard = () => {
  const [projects, setProjects] = useState([]);
  const [file, setFile] = useState(null);
  const [analysisResult, setAnalysisResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [uploadError, setUploadError] = useState(null);

  useEffect(() => {
    fetchProjects();
  }, []);

  const fetchProjects = async () => {
    try {
      const res = await getProjects();
      setProjects(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleFileUpload = async (e) => {
    e.preventDefault();
    if (!file) return;
    setLoading(true);
    setUploadError(null);
    setAnalysisResult(null);
    try {
      const res = await uploadFileForAnalysis(file);
      setAnalysisResult(res.data);
    } catch (err) {
      console.error(err);
      const detail = err.response?.data?.detail || err.message || 'Unknown error. Is the AI service running on port 8000?';
      setUploadError(detail);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dashboard-container">
      <h2>Your Projects</h2>
      <div className="projects-grid">
        {projects.map(p => (
          <Link key={p.id} to={`/projects/${p.id}`} className="project-card">
            <h3>{p.name}</h3>
            <p>{p.description}</p>
            <span className="status-badge">{p.status}</span>
          </Link>
        ))}
      </div>

      <div className="file-upload-section">
        <h3>AI File Import & Analysis</h3>
        <p className="subtitle">Upload your project data to get AI-driven insights and health scores.</p>
        <form onSubmit={handleFileUpload}>
          <div className="upload-zone">
            <div className="upload-icon">☁️</div>
            <div className="upload-text">{file ? file.name : "Drag & drop your file here or click to browse"}</div>
            <div className="upload-hint">Supports .json, .csv, .xlsx, .pdf</div>
            <input type="file" onChange={e => setFile(e.target.files[0])} accept=".json,.csv,.xlsx,.pdf" />
          </div>
          <div className="upload-actions">
            <button type="submit" disabled={loading || !file}>
              {loading ? 'Analyzing...' : 'Upload & Analyze'}
            </button>
          </div>
        </form>

        {uploadError && (
          <div className="upload-error">
            <strong>⚠ Analysis Failed:</strong> {uploadError}
          </div>
        )}

        {analysisResult && (
          <AIInsights health={analysisResult} />
        )}
      </div>
    </div>
  );
};

export default Dashboard;
