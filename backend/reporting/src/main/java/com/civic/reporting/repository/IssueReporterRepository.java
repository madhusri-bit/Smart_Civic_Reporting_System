package com.civic.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.civic.reporting.model.IssueReporter;

public interface IssueReporterRepository extends JpaRepository<IssueReporter, Long> {
}
