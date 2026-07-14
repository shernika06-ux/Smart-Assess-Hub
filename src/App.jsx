import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './Login';
import Dashboard from './Dashboard';

function App() {
    return (
        <Router>
            <Routes>
                {/* Default root hits login window first */}
                <Route path="/" element={<Login />} />

                {/* Secure dashboard panel mapped view */}
                <Route path="/dashboard" element={<Dashboard />} />
            </Routes>
        </Router>
    );
}

export default App;