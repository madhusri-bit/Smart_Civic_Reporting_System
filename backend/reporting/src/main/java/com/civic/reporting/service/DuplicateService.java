package com.civic.reporting.service;

import com.civic.reporting.model.Issue;

public interface DuplicateService {
    boolean isDuplicate(Issue issue);
}
