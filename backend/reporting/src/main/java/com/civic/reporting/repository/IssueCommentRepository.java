package com.civic.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.civic.reporting.model.IssueComment;

public interface IssueCommentRepository extends JpaRepository<IssueComment, Long> {
}
