package com.agentstudy.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI agentStudyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Agent Study API")
                        .version("0.1.0")
                        .description("基于大模型的个性化高数学习多智能体系统后端 API")
                        .license(new License().name("Portfolio Project")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("agent-study")
                .pathsToMatch("/api/**")
                .build();
    }
}
