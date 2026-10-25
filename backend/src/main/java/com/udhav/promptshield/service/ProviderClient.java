package com.udhav.promptshield.service;

import com.udhav.promptshield.domain.ProviderMode;

public interface ProviderClient {
    String respond(String prompt, ProviderMode mode);
}
