package com.example.deliverydemoapi.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the generated OpenAPI documentation.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Defines the top-level metadata of the OpenAPI document.
     *
     * @return the configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI deliveryDemoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Delivery Demo API")
                        .description("REST API for calculating delivery fees based on city, vehicle type and weather conditions.")
                        .version("0.0.1-SNAPSHOT"));
    }
}

