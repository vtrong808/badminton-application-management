package com.bsp.controller;

import com.bsp.dto.request.ShiftRequest;
import com.bsp.dto.response.ApiResponse;
import com.bsp.entity.ShiftSchedule;
import com.bsp.entity.User;
import com.bsp.entity.enums.ShiftType;
import com.bsp.repository.ShiftScheduleRepository;
import com.bsp.repository.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ShiftController {

    private final ShiftScheduleRepository shiftRepository;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BS')")
    public ResponseEntity<ApiResponse<List<ShiftSchedule>>> getAllShifts() {
        return ResponseEntity.ok(ApiResponse.success(shiftRepository.findAll(), "Thành công"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShiftSchedule>> assignShift(@RequestBody ShiftRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        ShiftSchedule shift = shiftRepository.findAll().stream()
                .filter(s -> s.getUser().getId().equals(user.getId()) && s.getShiftDate().equals(request.getShiftDate()))
                .findFirst()
                .orElse(new ShiftSchedule());

        shift.setUser(user);
        shift.setShiftDate(request.getShiftDate());
        shift.setShiftType(ShiftType.valueOf(request.getShiftType()));
        shift.setIsAttended(false);

        return ResponseEntity.ok(ApiResponse.success(shiftRepository.save(shift), "Phân ca thành công"));
    }

    @PutMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'BS')")
    public ResponseEntity<ApiResponse<ShiftSchedule>> checkIn(@PathVariable Long id) {
        ShiftSchedule shift = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca làm việc"));

        String currentUsername = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !shift.getUser().getUsername().equals(currentUsername)) {
            throw new RuntimeException("Lỗi: Không được phép điểm danh hộ người khác!");
        }

        shift.setIsAttended(true);
        shift.setCheckInTime(LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success(shiftRepository.save(shift), "Điểm danh thành công"));
    }
}