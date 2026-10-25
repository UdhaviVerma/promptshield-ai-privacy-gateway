package com.udhav.promptshield;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PromptshieldApplication {

    public static void main(String[] args) {
        normalizeRenderDatabaseUrl();
        SpringApplication.run(PromptshieldApplication.class, args);
    }

    private static void normalizeRenderDatabaseUrl() {
        String raw = System.getenv("DATABASE_URL");
        if (raw == null || raw.isBlank()) {
            return;
        }
        String jdbcUrl = raw;
        if (raw.startsWith("postgres://")) {
            jdbcUrl = "jdbc:postgresql://" + raw.substring("postgres://".length());
        } else if (raw.startsWith("postgresql://")) {
            jdbcUrl = "jdbc:postgresql://" + raw.substring("postgresql://".length());
        }
        System.setProperty("spring.datasource.url", jdbcUrl);
    }
}
