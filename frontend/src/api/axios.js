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

// Interceptor Response: Xử lý lỗi 401/403 toàn cục
api.interceptors.response.use(response => {
    return response;
}, error => {
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
        // Xóa sạch token cũ và bắt đăng nhập lại
        localStorage.removeItem('accessToken');
        localStorage.removeItem('user');
        window.location.href = '/login';
    }
    return Promise.reject(error);
});

export default api;