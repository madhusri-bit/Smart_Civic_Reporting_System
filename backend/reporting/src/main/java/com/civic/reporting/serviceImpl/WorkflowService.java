package com.civic.reporting.serviceImpl;

import org.springframework.stereotype.Service;
import com.civic.reporting.model.Issue;
import com.civic.reporting.model.IssueStatusHistory;
import com.civic.reporting.model.enumFolder.WorkflowStatus;
import com.civic.reporting.repository.IssueStatusHistoryRepository;
import java.time.LocalDateTime;

@Service
public class WorkflowService {

    private final IssueStatusHistoryRepository repo;

    public WorkflowService(IssueStatusHistoryRepository repo) {
        this.repo = repo;
    }

    public IssueStatusHistory add(Issue issue, WorkflowStatus status, String note) {
        IssueStatusHistory history = new IssueStatusHistory();
        history.setIssue(issue);
        history.setStatus(status);
        history.setNote(note);
        history.setCreatedAt(LocalDateTime.now());
        return repo.save(history);
    }
}
