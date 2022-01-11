package com.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI coreApi() {
        var info = new Info()
                .title("Online shop")
                .version("v0.2.0");

        return new OpenAPI()
                .info(info);
    }
}
