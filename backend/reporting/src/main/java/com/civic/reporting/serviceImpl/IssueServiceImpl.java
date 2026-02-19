package com.civic.reporting.serviceImpl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.civic.reporting.dto.IssueReportDTO;
import com.civic.reporting.model.Issue;
import com.civic.reporting.model.enumFolder.Status;
import com.civic.reporting.repository.IssueRepository;
import com.civic.reporting.service.IssueService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepo;
    private final ClassificationWorker classificationWorker;

    @Override
    public Issue reportIssue(IssueReportDTO dto) {
        Issue issue = new Issue();
        issue.setTitle(dto.getTitle());
        issue.setDescription(dto.getDescription());
        issue.setLatitude(dto.getLatitude());
        issue.setLongitude(dto.getLongitude());
        issue.setPhotoUrl(dto.getPhotoUrl());
        issue.setStatus(Status.PENDING_CLASSIFICATION);
        issue.setCreatedAt(LocalDateTime.now());

        Issue saved = issueRepo.save(issue);

        // Dispatch async classification
        classificationWorker.classify(saved.getId(), dto.getPhotoUrl(), dto.getLatitude(), dto.getLongitude());

        return saved;
    }
}
