package com.civic.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.civic.reporting.model.IssueStatusHistory;

public interface IssueStatusHistoryRepository extends JpaRepository<IssueStatusHistory, Long> {
}
