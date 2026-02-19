package com.civic.reporting.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.civic.reporting.dto.IssueReportDTO;
import com.civic.reporting.model.Issue;
import com.civic.reporting.service.IssueService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    @PostMapping("/report")
    public Issue reportIssue(@RequestBody IssueReportDTO dto) {
        return issueService.reportIssue(dto);
    }
}