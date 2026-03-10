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

import java.time.LocalDate;
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
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN') or hasAuthority('ROLE_BS') or hasAuthority('BS')")
    public ResponseEntity<ApiResponse<List<ShiftSchedule>>> getAllShifts() {
        return ResponseEntity.ok(ApiResponse.success(shiftRepository.findAll(), "Thành công"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<ShiftSchedule>> assignShift(@RequestBody ShiftRequest request) {

        if (request.getUserId() == null) {
            throw new RuntimeException("Thiếu thông tin ID nhân viên!");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // KỸ THUẬT PARSE NGÀY THÁNG LINH HOẠT
        LocalDate parsedDate;
        try {
            // Mặc định dịch theo chuẩn YYYY-MM-DD của thẻ input type="date"
            parsedDate = LocalDate.parse(request.getShiftDate());
        } catch (Exception e) {
            try {
                // Đề phòng Frontend gửi dạng DD/MM/YYYY
                parsedDate = LocalDate.parse(request.getShiftDate(), java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception ex) {
                throw new RuntimeException("Sai định dạng ngày: " + request.getShiftDate());
            }
        }

        ShiftSchedule shift = shiftRepository.findByUserIdAndShiftDate(user.getId(), parsedDate)
                .orElse(new ShiftSchedule());

        shift.setUser(user);
        shift.setShiftDate(parsedDate);
        shift.setShiftType(ShiftType.valueOf(request.getShiftType()));
        shift.setIsAttended(false);

        return ResponseEntity.ok(ApiResponse.success(shiftRepository.save(shift), "Phân ca thành công"));
    }

    @PutMapping("/{id}/check-in")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN') or hasAuthority('ROLE_BS') or hasAuthority('BS')")
    public ResponseEntity<ApiResponse<ShiftSchedule>> checkIn(@PathVariable Long id) {
        ShiftSchedule shift = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca làm việc"));

        String currentUsername = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

        if (!isAdmin && !shift.getUser().getUsername().equals(currentUsername)) {
            throw new RuntimeException("Lỗi: Không được phép điểm danh hộ người khác!");
        }

        shift.setIsAttended(true);
        shift.setCheckInTime(LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success(shiftRepository.save(shift), "Điểm danh thành công"));
    }
}