package com.civic.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.civic.reporting.model.Escalation;

public interface EscalationRepository extends JpaRepository<Escalation, Long> {

}
