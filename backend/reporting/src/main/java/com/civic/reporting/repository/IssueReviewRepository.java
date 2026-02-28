package com.civic.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.civic.reporting.model.IssueReview;

public interface IssueReviewRepository extends JpaRepository<IssueReview, Long> {
}
