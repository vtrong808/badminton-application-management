import { defineStore } from 'pinia';
import api from '../api/axios';

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('accessToken') || null,
        user: JSON.parse(localStorage.getItem('user')) || null,
    }),

    getters: {
        isAuthenticated: (state) => !!state.token,
        getUserRole: (state) => state.user ? state.user.role : null,
        getUsername: (state) => state.user ? state.user.username : '',
    },

    actions: {
        async login(username, password) {
            try {
                // Backend trả về thẳng AuthResponse { accessToken, refreshToken, username, role }
                const response = await api.post('/auth/login', { username, password });

                this.token = response.data.accessToken;
                this.user = {
                    username: response.data.username,
                    role: response.data.role
                };

                // Lưu vào LocalStorage để F5 không bị mất đăng nhập
                localStorage.setItem('accessToken', this.token);
                localStorage.setItem('user', JSON.stringify(this.user));

                return true;
            } catch (error) {
                console.error('Login failed:', error);
                throw error;
            }
        },

        async logout() {
            try {
                // Gọi API backend để đưa token vào Redis Blacklist
                await api.post('/auth/logout');
            } catch (error) {
                console.error('Lỗi khi gọi API logout:', error);
            } finally {
                // Xóa state dù backend có lỗi hay không
                this.token = null;
                this.user = null;
                localStorage.removeItem('accessToken');
                localStorage.removeItem('user');
            }
        }
    }
});