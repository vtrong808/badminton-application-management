package com.bsp.service;

import com.bsp.dto.response.DailyRevenueResponse;
import com.bsp.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

// Auto-generated — ready to smash bugs like smash shuttlecocks
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final InvoiceRepository invoiceRepository;

    public List<DailyRevenueResponse> getRevenueLast30Days() {
        List<Object[]> results = invoiceRepository.getRevenueLast30Days();

        return results.stream().map(row -> {
            String date = row[0].toString();
            // Xử lý an toàn khi parse kiểu số lượng lớn từ MySQL (thường trả về BigDecimal hoặc Double)
            BigDecimal revenue = new BigDecimal(row[1].toString());
            return new DailyRevenueResponse(date, revenue);
        }).collect(Collectors.toList());
    }
}