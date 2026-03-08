package com.bsp.controller;

import com.bsp.dto.request.CustomerRequest;
import com.bsp.dto.response.ApiResponse;
import com.bsp.entity.Customer;
import com.bsp.entity.enums.CustomerType;
import com.bsp.repository.CustomerRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder; // Thêm mã hóa

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BS')")
    public ResponseEntity<ApiResponse<List<Customer>>> getAllCustomers() {
        return ResponseEntity.ok(ApiResponse.success(customerRepository.findAll(), "Thành công"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BS')")
    public ResponseEntity<ApiResponse<Customer>> createCustomer(@RequestBody CustomerRequest request) {
        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword())) // Mã hóa mật khẩu
                .customerType(CustomerType.valueOf(request.getCustomerType())) // Set loại khách
                .build();
        return ResponseEntity.ok(ApiResponse.success(customerRepository.save(customer), "Tạo khách hàng thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BS')")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(@PathVariable Long id, @RequestBody CustomerRequest request) {
        Customer customer = customerRepository.findById(id).orElseThrow();
        customer.setFullName(request.getFullName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setCustomerType(CustomerType.valueOf(request.getCustomerType()));

        // Nếu BS có nhập mật khẩu mới thì mới cập nhật password
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return ResponseEntity.ok(ApiResponse.success(customerRepository.save(customer), "Cập nhật thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BS')")
    public ResponseEntity<ApiResponse<String>> deleteCustomer(@PathVariable Long id) {
        customerRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("OK", "Xóa khách hàng thành công"));
    }
}