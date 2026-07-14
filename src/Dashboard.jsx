import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import API from './api';

function Dashboard() {
    const [tasks, setTasks] = useState([]);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        const fetchTasksFromDb = async () => {
            try {
                // Connecting to TaskController.java endpoint via Interceptor
                const response = await API.get('/tasks');
                setTasks(response.data);
            } catch (err) {
                setError('🚫 Access Denied: Please log in with valid credentials to view this workspace.');
                sessionStorage.removeItem('token');
            }
        };
        fetchTasksFromDb();
    }, []);

    const handleLogout = () => {
        sessionStorage.removeItem('token');
        navigate('/');
    };

    return (
        <div style={{
            minHeight: '100vh', background: '#f8fafc',
            fontFamily: '"Segoe UI", Roboto, sans-serif', padding: '30px'
        }}>
            {error ? (
                <div style={{
                    maxWidth: '500px', margin: '100px auto', background: 'white',
                    padding: '30px', borderRadius: '12px', boxShadow: '0 4px 15px rgba(0,0,0,0.1)',
                    textAlign: 'center'
                }}>
                    <h3 style={{ color: '#dc2626', marginBottom: '15px' }}>Authentication Error</h3>
                    <p style={{ color: '#64748b', fontSize: '15px', lineHeight: '1.6' }}>{error}</p>
                    <button
                        onClick={() => navigate('/')}
                        style={{ marginTop: '20px', padding: '10px 20px', background: '#4f46e5', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: '600' }}
                    >
                        Go to Login
                    </button>
                </div>
            ) : (
                <div style={{ maxWidth: '1000px', margin: '0 auto' }}>

                    <div style={{
                        display: 'flex', alignItems: 'center',
                        background: 'white', padding: '20px 30px', borderRadius: '12px',
                        boxShadow: '0 2px 8px rgba(0,0,0,0.05)', marginBottom: '30px',
                        justifyContent: 'space-between'
                    }}>
                        <div>
                            <h2 style={{ color: '#1e293b', margin: 0 }}>📊 Project Workspace</h2>
                            <p style={{ color: '#64748b', margin: '5px 0 0 0', fontSize: '14px' }}>Welcome back, Admin! Secure session is active.</p>
                        </div>
                        <button
                            onClick={handleLogout}
                            style={{
                                padding: '10px 20px', background: '#ef4444', color: 'white',
                                border: 'none', borderRadius: '8px', fontWeight: 'bold',
                                cursor: 'pointer', transition: 'background 0.3s'
                            }}
                        >
                            Logout
                        </button>
                    </div>

                    <div style={{ background: 'white', padding: '30px', borderRadius: '12px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}>
                        <h3 style={{ color: '#334155', borderBottom: '2px solid #f1f5f9', paddingBottom: '15px', marginBottom: '20px' }}>
                            📋 Current Database Sync Tasks
                        </h3>

                        <div style={{ overflowX: 'auto' }}>
                            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                                <thead>
                                <tr style={{ background: '#f8fafc', color: '#64748b' }}>
                                    <th style={{ padding: '12px 15px', borderBottom: '2px solid #e2e8f0', fontWeight: '600' }}>S.No</th>
                                    <th style={{ padding: '12px 15px', borderBottom: '2px solid #e2e8f0', fontWeight: '600' }}>Task Description</th>
                                    <th style={{ padding: '12px 15px', borderBottom: '2px solid #e2e8f0', fontWeight: '600' }}>Status</th>
                                </tr>
                                </thead>
                                <tbody>
                                {tasks.map((task, index) => (
                                    <tr key={index} style={{ borderBottom: '1px solid #f1f5f9' }}>
                                        <td style={{ padding: '15px', color: '#64748b', fontWeight: '500' }}>{index + 1}</td>
                                        <td style={{ padding: '15px', color: '#334155', fontWeight: '500' }}>{task}</td>
                                        <td style={{ padding: '15px' }}>
                                               <span style={{ background: '#dcfce7', color: '#15803d', padding: '4px 10px', borderRadius: '20px', fontSize: '12px', fontWeight: '600' }}>
                                                   Active Sync
                                               </span>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Dashboard;