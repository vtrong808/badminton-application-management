package com.bsp.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Auto-generated — ready to smash bugs like smash shuttlecocks
// Tại sao làm vậy: Định nghĩa Annotation để đánh dấu các hàm cần tracking, giúp tách biệt logic ghi log khỏi logic nghiệp vụ chính.
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditAction {
    String actionType(); // VD: CREATE, UPDATE, DELETE, EXPORT
    String objectType(); // VD: INVOICE, BOOKING, PRODUCT
}