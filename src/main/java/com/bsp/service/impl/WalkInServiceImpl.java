package com.bsp.service.impl;

import com.bsp.dto.request.WalkInRequest;
import com.bsp.entity.WalkInSession;
import com.bsp.entity.enums.WalkInStatus;
import com.bsp.repository.WalkInSessionRepository;
import com.bsp.service.WalkInService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalkInServiceImpl implements WalkInService {

    private final WalkInSessionRepository walkInRepository;

    @Override
    public List<WalkInSession> getTodaySessions() {
        // GIỮ NGUYÊN LOGIC
        return walkInRepository.findAll().stream()
                .filter(s -> s.getPlayDate().equals(LocalDate.now()))
                .toList();
    }

    @Override
    public WalkInSession createSession(WalkInRequest request) {

        WalkInSession session = WalkInSession.builder()
                .customerName(request.getCustomerName())
                .phoneNumber(request.getPhoneNumber())
                .age(request.getAge())
                .playDate(LocalDate.now())
                .startTime(LocalTime.now())
                .price(request.getPrice() != null
                        ? request.getPrice()
                        : new BigDecimal("50000"))
                .status(WalkInStatus.PLAYING)
                .build();

        return walkInRepository.save(session);
    }

    @Override
    public WalkInSession finishSession(Long id) {

        WalkInSession session = walkInRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên chơi"));

        session.setStatus(WalkInStatus.FINISHED);

        return walkInRepository.save(session);
    }
}