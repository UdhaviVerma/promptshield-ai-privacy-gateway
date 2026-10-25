package com.udhav.promptshield.controller;

import com.udhav.promptshield.api.InspectRequest;
import com.udhav.promptshield.api.InspectResponse;
import com.udhav.promptshield.service.InspectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InspectController {

    private final InspectService inspectService;

    public InspectController(InspectService inspectService) {
        this.inspectService = inspectService;
    }

    @PostMapping("/inspect")
    public InspectResponse inspect(@Valid @RequestBody InspectRequest request) {
        return inspectService.inspect(request);
    }
}
