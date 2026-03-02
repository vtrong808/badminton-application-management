package com.bsp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Auto-generated — ready to smash bugs like smash shuttlecocks
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "BSP System API V2", version = "v2.0"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class BadmintonManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(BadmintonManagementApplication.class, args);
    }
}