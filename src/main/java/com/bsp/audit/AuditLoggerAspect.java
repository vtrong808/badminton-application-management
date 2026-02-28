package com.bsp.audit;

import com.bsp.entity.AuditLog;
import com.bsp.entity.User;
import com.bsp.repository.AuditLogRepository;
import com.bsp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Auto-generated — ready to smash bugs like smash shuttlecocks
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLoggerAspect {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    // Tại sao làm vậy: Chạy hàm này NGAY SAU KHI một hàm có gắn @AuditAction thực thi thành công.
    // Nếu hàm lỗi (throw Exception), nó sẽ không ghi log để tránh rác DB.
    @AfterReturning(value = "@annotation(auditAction)", returning = "result")
    public void logAfterExecution(JoinPoint joinPoint, AuditAction auditAction, Object result) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long actorId = null;

        // Tìm User hiện tại để lấy ID
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            actorId = userOpt.get().getId();
        }

        // Lấy ID của Object vừa bị thao tác (Giả định đối số đầu tiên của hàm là ID)
        Object[] args = joinPoint.getArgs();
        Long objectId = (args != null && args.length > 0 && args[0] instanceof Long) ? (Long) args[0] : 0L;

        AuditLog log = AuditLog.builder()
                .actorId(actorId)
                .actionType(auditAction.actionType())
                .objectType(auditAction.objectType())
                .objectId(objectId)
                .timestamp(LocalDateTime.now())
                .details("Executed method: " + joinPoint.getSignature().getName())
                .build();

        auditLogRepository.save(log);
    }
}