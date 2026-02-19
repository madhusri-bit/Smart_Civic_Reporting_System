package com.civic.reporting.service;

import com.civic.reporting.model.Issue;

public interface PriorityService {
    double calculatePriority(Issue issue);
}
