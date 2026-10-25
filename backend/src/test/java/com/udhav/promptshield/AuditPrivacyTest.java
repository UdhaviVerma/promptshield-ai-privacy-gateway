package com.udhav.promptshield;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udhav.promptshield.repository.AuditEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuditPrivacyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditEntryRepository auditEntryRepository;

    @Test
    void auditDoesNotStoreRawPromptOrValues() throws Exception {
        String prompt = "Reach me at jane@example.com and +1 202-555-0199";
        Map<String, Object> payload = Map.of("prompt", prompt, "mode", "DEMO");

        mockMvc.perform(post("/api/inspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        var latest = auditEntryRepository.findTop100ByOrderByCreatedAtDesc();
        assertThat(latest).isNotEmpty();
        String row = latest.get(0).getMatchedCategories() + latest.get(0).getMatchedRuleIds();
        assertThat(row).doesNotContain("jane@example.com");
        assertThat(row).doesNotContain("202-555-0199");
    }
}
