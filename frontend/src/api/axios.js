import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080/api/v1', // Trỏ thẳng vào Spring Boot của em
    headers: {
        'Content-Type': 'application/json',
    }
});

// Interceptor Request: Tự động đính kèm Token
api.interceptors.request.use(config => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

api.interceptors.response.use(response => {
    return response;
}, error => {
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
        // CHỈ đá văng nếu API bị lỗi KHÔNG PHẢI là API login
        // và người dùng KHÔNG PHẢI đang đứng ở trang login
        if (error.config.url !== '/auth/login' && window.location.pathname !== '/login') {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('user');
            window.location.href = '/login';
        }
    }
    return Promise.reject(error);
});

export default api;