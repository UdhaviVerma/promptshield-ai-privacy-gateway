package com.udhav.promptshield;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PromptshieldApplication {

    public static void main(String[] args) {
        normalizeDatasourceConfig();
        SpringApplication.run(PromptshieldApplication.class, args);
    }

    private static void normalizeDatasourceConfig() {
        String configuredUrl = firstNonBlank(
                System.getenv("SPRING_DATASOURCE_URL"),
                System.getenv("DATABASE_URL"),
                System.getProperty("spring.datasource.url"));

        if (isBlank(configuredUrl)) {
            return;
        }

        ParsedDatasource parsed = parseDatasourceUrl(configuredUrl);
        if (!isBlank(parsed.jdbcUrl())) {
            System.setProperty("spring.datasource.url", parsed.jdbcUrl());
        }

        if (isBlank(System.getenv("SPRING_DATASOURCE_USERNAME"))
                && isBlank(System.getProperty("spring.datasource.username"))
                && !isBlank(parsed.username())) {
            System.setProperty("spring.datasource.username", parsed.username());
        }

        if (isBlank(System.getenv("SPRING_DATASOURCE_PASSWORD"))
                && isBlank(System.getProperty("spring.datasource.password"))
                && !isBlank(parsed.password())) {
            System.setProperty("spring.datasource.password", parsed.password());
        }
    }

    private static ParsedDatasource parseDatasourceUrl(String rawUrl) {
        String trimmed = rawUrl.trim();

        if (trimmed.startsWith("jdbc:postgresql://") && !trimmed.contains("@")) {
            return new ParsedDatasource(trimmed, null, null);
        }

        String uriCandidate;
        if (trimmed.startsWith("jdbc:postgresql://")) {
            uriCandidate = trimmed.substring("jdbc:".length());
        } else if (trimmed.startsWith("postgresql://") || trimmed.startsWith("postgres://")) {
            uriCandidate = trimmed.replaceFirst("^postgres://", "postgresql://");
        } else {
            return new ParsedDatasource(trimmed, null, null);
        }

        try {
            URI uri = URI.create(uriCandidate);
            if (!"postgresql".equalsIgnoreCase(uri.getScheme()) || isBlank(uri.getHost()) || isBlank(uri.getPath())) {
                return new ParsedDatasource(trimmed, null, null);
            }

            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath();
            if (!isBlank(uri.getQuery())) {
                jdbcUrl += "?" + uri.getQuery();
            }

            String username = null;
            String password = null;
            if (!isBlank(uri.getUserInfo())) {
                String[] creds = uri.getUserInfo().split(":", 2);
                username = urlDecode(creds[0]);
                if (creds.length > 1) {
                    password = urlDecode(creds[1]);
                }
            }

            return new ParsedDatasource(jdbcUrl, username, password);
        } catch (IllegalArgumentException ex) {
            return new ParsedDatasource(trimmed, null, null);
        }
    }

    private static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record ParsedDatasource(String jdbcUrl, String username, String password) {
    }
}
