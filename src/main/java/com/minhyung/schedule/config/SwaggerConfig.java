package com.minhyung.schedule.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("schedule-sharing-service-v1")
                .pathsToMatch("/api/v1/**")
                .build();
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Schedule Sharing Service API")
                        .version("v1.0.0")
                        .description("일정 공유 서비스 API")
                ).externalDocs(new ExternalDocumentation()
                        .description("에러 코드 전체 목록은 여기를 참고하세요.")
                        .url("https://docs.google.com/spreadsheets/d/12WSgsgbXCwYlu5p4H0jHIue0zceXF-0WW2W6EiX5UFI/edit?gid=784980039#gid=784980039")
                );
    }
}
