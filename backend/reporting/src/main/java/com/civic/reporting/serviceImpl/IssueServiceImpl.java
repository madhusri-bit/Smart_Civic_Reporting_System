package com.civic.reporting.serviceImpl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.civic.reporting.dto.IssueReportDTO;
import com.civic.reporting.model.Issue;
import com.civic.reporting.model.enumFolder.Category;
import com.civic.reporting.model.enumFolder.Status;
import com.civic.reporting.repository.IssueRepository;
import com.civic.reporting.service.IssueService;
import com.civic.reporting.service.VisionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepo;
    private final VisionService visionService;

    @Override
    public Issue reportIssue(IssueReportDTO dto) {
        Issue issue = new Issue();
        issue.setTitle(dto.getTitle());
        issue.setDescription(dto.getDescription());
        issue.setLatitude(dto.getLatitude());
        issue.setLongitude(dto.getLongitude());
        issue.setStatus(Status.REPORTED);
        issue.setCreatedAt(LocalDateTime.now());

        String detectedCategory = visionService.detectCategory(dto.getPhotoUrl());

        issue.setCategory(Category.valueOf(detectedCategory.toUpperCase()));

        return issueRepo.save(issue);
    }
}
