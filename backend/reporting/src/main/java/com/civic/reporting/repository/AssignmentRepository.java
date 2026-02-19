package com.civic.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.civic.reporting.model.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
}