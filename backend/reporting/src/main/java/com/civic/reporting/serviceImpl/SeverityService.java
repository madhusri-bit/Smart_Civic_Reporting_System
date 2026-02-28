package com.civic.reporting.serviceImpl;

import org.springframework.stereotype.Service;
import com.civic.reporting.model.Issue;
import com.civic.reporting.repository.IssueVoteRepository;
import com.civic.reporting.repository.IssueCommentRepository;
import java.util.stream.Collectors;

@Service
public class SeverityService {

    private final IssueVoteRepository voteRepo;
    private final IssueCommentRepository commentRepo;

    public SeverityService(IssueVoteRepository voteRepo, IssueCommentRepository commentRepo) {
        this.voteRepo = voteRepo;
        this.commentRepo = commentRepo;
    }

    public long countVotes(Issue issue) {
        return voteRepo.findAll().stream()
                .filter(v -> v.getIssue() != null && v.getIssue().getId().equals(issue.getId()))
                .collect(Collectors.counting());
    }

    public long countComments(Issue issue) {
        return commentRepo.findAll().stream()
                .filter(c -> c.getIssue() != null && c.getIssue().getId().equals(issue.getId()))
                .collect(Collectors.counting());
    }

    public double computeSeverity(Issue issue) {
        double base = issue.getPriorityScore() == null ? 50.0 : issue.getPriorityScore();
        long upvotes = countVotes(issue);
        long comments = countComments(issue);
        return base + (upvotes * 2.0) + (comments * 1.0);
    }
}
