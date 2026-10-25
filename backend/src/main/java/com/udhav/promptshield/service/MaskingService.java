package com.udhav.promptshield.service;

import com.udhav.promptshield.domain.DataCategory;
import org.springframework.stereotype.Service;

@Service
public class MaskingService {

    public String maskValue(DataCategory category, String value) {
        return switch (category) {
            case EMAIL -> "[EMAIL_REDACTED]";
            case PHONE -> "[PHONE_REDACTED]";
            case PAYMENT_CARD -> "[PAYMENT_CARD_REDACTED]";
            case API_KEY -> "[API_KEY_REDACTED]";
        };
    }

    public String applyMask(String original, DetectionMatch match) {
        String prefix = original.substring(0, match.start());
        String suffix = original.substring(match.end());
        return prefix + maskValue(match.category(), match.value()) + suffix;
    }
}
