package com.civic.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.civic.reporting.model.IssueResolution;

public interface IssueResolutionRepository extends JpaRepository<IssueResolution, Long> {
}
