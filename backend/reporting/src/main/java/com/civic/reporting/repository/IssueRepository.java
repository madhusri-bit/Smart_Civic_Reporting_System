package com.civic.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.civic.reporting.model.Issue;

public interface IssueRepository extends JpaRepository<Issue, Long> {
}