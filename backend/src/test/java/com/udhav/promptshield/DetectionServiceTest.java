package com.udhav.promptshield;

import com.udhav.promptshield.domain.DataCategory;
import com.udhav.promptshield.service.DetectionMatch;
import com.udhav.promptshield.service.DetectionService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DetectionServiceTest {

    private final DetectionService detectionService = new DetectionService();

    @Test
    void detectsExpectedCategoriesAndUsesLuhnForCards() {
        String prompt = "email jane@example.com phone +1 202-555-0199 key sk-123456789012345678901234 card 4111 1111 1111 1111 bad 4111 1111 1111 1112";

        List<DetectionMatch> matches = detectionService.detect(prompt);

        assertThat(matches).extracting(DetectionMatch::category)
                .contains(DataCategory.EMAIL, DataCategory.PHONE, DataCategory.API_KEY, DataCategory.PAYMENT_CARD);

        assertThat(matches.stream().filter(m -> m.category() == DataCategory.PAYMENT_CARD).count())
                .isEqualTo(1);
    }
}
