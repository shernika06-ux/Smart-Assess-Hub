import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import API from './api';

function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const response = await API.post('/auth/login', { username, password });
            const data = response.data;
            sessionStorage.setItem('token', data.token);
            sessionStorage.setItem('role', data.role);
            sessionStorage.setItem('fullName', data.fullName);
            sessionStorage.setItem('userId', data.id);
            navigate('/dashboard');
        } catch (err) {
            console.error(err);
            if (err.response && err.response.data && err.response.data.error) {
                setError(`❌ ${err.response.data.error}`);
            } else {
                setError('❌ Invalid credentials or server is offline!');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: 'linear-gradient(135deg, #1e293b 0%, #0f172a 100%)', fontFamily: 'sans-serif' }}>
            <div style={{ background: '#ffffff', padding: '40px', borderRadius: '12px', boxShadow: '0 8px 20px rgba(0,0,0,0.3)', width: '320px', textAlign: 'center' }}>
                <h2 style={{ margin: '0 0 5px 0', color: '#1e293b' }}>SXCCE</h2>
                <p style={{ margin: '0 0 20px 0', color: '#475569', fontSize: '14px', fontWeight: 'bold' }}>St. Xavier's Catholic College of Engineering</p>
                <form onSubmit={handleLogin} style={{ textAlign: 'left' }}>
                    <div style={{ margin: '0 0 15px 0' }}>
                        <label style={{ fontWeight: 'bold', color: '#475569' }}>Username</label>
                        <input type="text" value={username} onChange={(e) => setUsername(e.target.value)} style={{ width: '100%', padding: '10px', marginTop: '5px', borderRadius: '6px', border: '1px solid #cbd5e1', boxSizing: 'border-box' }} placeholder="Enter admin" required />
                    </div>
                    <div style={{ margin: '0 0 20px 0' }}>
                        <label style={{ fontWeight: 'bold', color: '#475569' }}>Password</label>
                        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} style={{ width: '100%', padding: '10px', marginTop: '5px', borderRadius: '6px', border: '1px solid #cbd5e1', boxSizing: 'border-box' }} placeholder="Enter admin123" required />
                    </div>
                    <button type="submit" disabled={loading} style={{ width: '100%', padding: '12px', background: '#3b82f6', color: 'white', border: 'none', borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer' }}>
                        {loading ? 'Logging in...' : 'Sign In'}
                    </button>
                </form>
                {error && <p style={{ color: '#ef4444', marginTop: '15px', fontWeight: 'bold' }}>{error}</p>}
            </div>
        </div>
    );
}

export default Login;