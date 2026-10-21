package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI qlbhOpenApi() {
        return new OpenAPI().info(new Info()
                .title("QLBH API")
                .version("1.0")
                .description("API quan ly goi dich vu, don hang va kich hoat dich vu"));
    }
}
