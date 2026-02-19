package com.civic.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.civic.reporting.model.IssueVote;

public interface IssueVoteRepository extends JpaRepository<IssueVote, Long> {
}