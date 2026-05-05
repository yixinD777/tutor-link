package com.tutorlink.service.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "ai.advisor")
@Data
public class AiAdvisorConfig {

    private String apiKey;
    private String model = "mimo-v2.5-pro[1m]";
    private String baseUrl = "https://token-plan-cn.xiaomimimo.com/anthropic";
    private int maxTokens = 2048;
    private int timeoutSeconds = 30;

    @Bean("claudeRestTemplate")
    public RestTemplate claudeRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        return new RestTemplate(factory);
    }
}
