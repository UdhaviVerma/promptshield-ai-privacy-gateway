package com.udhav.promptshield.service;

import com.udhav.promptshield.domain.ProviderMode;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class DemoProviderClient implements ProviderClient {

    @Override
    public String respond(String prompt, ProviderMode mode) {
        if (mode == ProviderMode.DEMO) {
            return "DEMO_PROVIDER_RESPONSE: synthetic summary for prompt fingerprint " + fingerprint(prompt);
        }
        return "Provider mode was requested, but no provider is configured. Set PROVIDER_ENABLED=true and secure provider variables.";
    }

    private String fingerprint(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                out.append(String.format("%02x", hash[i]));
            }
            return out.toString();
        } catch (NoSuchAlgorithmException e) {
            return "unknown";
        }
    }
}
