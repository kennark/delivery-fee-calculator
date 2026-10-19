package com.example.deliverydemoapi.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${weather.api.base-url}")
    private String url;

    /**
     * Creates a {@link RestClient} bean preconfigured with the weather API base URL.
     *
     * @return a configured {@link RestClient} instance
     */
    @Bean
    RestClient restClient() {

        return RestClient
                .builder()
                .baseUrl(url)
                .build();
    }

}
