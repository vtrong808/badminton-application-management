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
                const response = await api.post('/auth/login', { username, password });

                const payload = response.data.data || response.data;

                this.token = payload.accessToken;
                this.user = {
                    username: payload.username,
                    role: payload.role
                };

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
                await api.post('/auth/logout');
            } catch (error) {
                console.error('Lỗi khi gọi API logout:', error);
            } finally {
                this.token = null;
                this.user = null;
                localStorage.removeItem('accessToken');
                localStorage.removeItem('user');
            }
        }
    }
});