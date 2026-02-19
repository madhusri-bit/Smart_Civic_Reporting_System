package com.civic.reporting.serviceImpl;

import org.springframework.stereotype.Service;

import com.civic.reporting.model.Issue;
import com.civic.reporting.service.PriorityService;

@Service
public class PriorityServiceImpl implements PriorityService {

    @Override
    public double calculatePriority(Issue issue) {
        return 75.0;
    }
}
