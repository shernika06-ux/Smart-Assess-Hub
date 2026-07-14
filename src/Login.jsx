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
            // hitting AuthController.java inside backend
            const response = await API.post('/auth/login', { username, password });
            sessionStorage.setItem('token', response.data.token); // Browser browser storage la token store aagum
            navigate('/dashboard'); // dashboard protected page-ku route panni kootitu pogum
        } catch (err) {
            setError('❌ Wrong username or password, bro! Try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{
            display: 'flex', justifyContent: 'center', alignItems: 'center',
            height: '100vh', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            fontFamily: '"Segoe UI", Roboto, sans-serif'
        }}>
            <div style={{
                background: '#ffffff', padding: '40px', borderRadius: '16px',
                boxShadow: '0 10px 25px rgba(0,0,0,0.2)', width: '350px', textAlign: 'center'
            }}>
                <h2 style={{ color: '#333', marginBottom: '10px' }}>🔐 Welcome Back</h2>
                <p style={{ color: '#777', fontSize: '14px', marginBottom: '30px' }}>Sign in to access your secure task manager</p>

                <form onSubmit={handleLogin} style={{ textAlign: 'left' }}>
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ display: 'block', fontWeight: '600', marginBottom: '8px', color: '#555' }}>Username</label>
                        <input
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            placeholder="Enter admin"
                            style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #ccc', boxSizing: 'border-box', fontSize: '15px' }}
                            required
                        />
                    </div>
                    <div style={{ marginBottom: '25px' }}>
                        <label style={{ display: 'block', fontWeight: '600', marginBottom: '8px', color: '#555' }}>Password</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="Enter admin123"
                            style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #ccc', boxSizing: 'border-box', fontSize: '15px' }}
                            required
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        style={{
                            width: '100%', padding: '12px', background: '#4f46e5', color: 'white',
                            border: 'none', borderRadius: '8px', fontWeight: 'bold', fontSize: '16px',
                            cursor: 'pointer', transition: 'background 0.3s ease',
                            opacity: loading ? 0.7 : 1
                        }}
                    >
                        {loading ? 'Authenticating...' : 'Sign In'}
                    </button>
                </form>

                {error && (
                    <div style={{
                        marginTop: '20px', padding: '10px', background: '#fee2e2',
                        color: '#991b1b', borderRadius: '8px', fontSize: '14px', fontWeight: '500'
                    }}>
                        {error}
                    </div>
                )}
            </div>
        </div>
    );
}

export default Login;