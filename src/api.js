import axios from 'axios';

// Namaloda Spring Boot Back-end standard local port code mapping
const API = axios.create({
    baseURL: 'http://localhost:8080/api',
});

// Ovvoru request anupumbothum session storage-la token irundha eduthu header la inject panra logic
API.interceptors.request.use((config) => {
    const token = sessionStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

export default API;