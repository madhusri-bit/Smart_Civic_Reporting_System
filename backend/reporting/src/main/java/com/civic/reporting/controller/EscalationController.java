package com.civic.reporting.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.civic.reporting.model.Escalation;
import com.civic.reporting.repository.EscalationRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/escalations")
@RequiredArgsConstructor
public class EscalationController {

    private final EscalationRepository repo;

    @PostMapping
    public Escalation escalate(@RequestBody Escalation esc) {
        return repo.save(esc);
    }
}
