package com.bsp.config;

import com.bsp.entity.Customer;
import com.bsp.entity.User;
import com.bsp.repository.CustomerRepository;
import com.bsp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository; // Bổ sung Repo Khách hàng

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // 1. Quét trong bảng Users (Dành cho Admin, BS)
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                return userOpt.get();
            }

            // 2. Quét trong bảng Customers (Dành cho Khách hàng đăng nhập bằng SĐT)
            Optional<Customer> customerOpt = customerRepository.findByPhoneNumber(username);
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                // Vì Customer không implement UserDetails, ta phải bọc nó vào class User của Spring
                return org.springframework.security.core.userdetails.User.builder()
                        .username(customer.getPhoneNumber())
                        .password(customer.getPassword() != null ? customer.getPassword() : "")
                        .roles("CUSTOMER") // Đóng dấu quyền ROLE_CUSTOMER
                        .build();
            }

            throw new UsernameNotFoundException("Không tìm thấy tài khoản với Username/SĐT: " + username);
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}