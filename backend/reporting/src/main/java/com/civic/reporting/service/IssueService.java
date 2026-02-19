package com.civic.reporting.service;

import com.civic.reporting.dto.IssueReportDTO;
import com.civic.reporting.model.Issue;

public interface IssueService {
    Issue reportIssue(IssueReportDTO dto);
}
