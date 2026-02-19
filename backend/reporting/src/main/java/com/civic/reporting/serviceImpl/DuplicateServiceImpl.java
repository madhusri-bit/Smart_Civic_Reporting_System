package com.civic.reporting.serviceImpl;

import org.springframework.stereotype.Service;

import com.civic.reporting.model.Issue;
import com.civic.reporting.service.DuplicateService;

@Service
public class DuplicateServiceImpl implements DuplicateService {

    @Override
    public boolean isDuplicate(Issue issue) {
        return false;
    }
}
