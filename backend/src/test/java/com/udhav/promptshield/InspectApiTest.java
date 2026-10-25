package com.udhav.promptshield;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InspectApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void blocksCardByDefaultAndReturnsSafeFields() throws Exception {
        Map<String, Object> payload = Map.of(
                "prompt", "Charge this test card 4111 1111 1111 1111 for me",
                "mode", "DEMO"
        );

        mockMvc.perform(post("/api/inspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalAction").value("BLOCK"))
                .andExpect(jsonPath("$.blocked").value(true))
                .andExpect(jsonPath("$.maskedPrompt").value(containsString("[PAYMENT_CARD_REDACTED]")));
    }

    @Test
    void validatesInput() throws Exception {
        Map<String, Object> payload = Map.of(
                "prompt", "abc",
                "mode", "DEMO"
        );

        mockMvc.perform(post("/api/inspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
