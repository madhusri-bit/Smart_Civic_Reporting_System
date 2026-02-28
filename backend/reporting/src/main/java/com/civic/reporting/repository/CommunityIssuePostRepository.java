package com.civic.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.civic.reporting.model.CommunityIssuePost;

public interface CommunityIssuePostRepository extends JpaRepository<CommunityIssuePost, Long> {
}
