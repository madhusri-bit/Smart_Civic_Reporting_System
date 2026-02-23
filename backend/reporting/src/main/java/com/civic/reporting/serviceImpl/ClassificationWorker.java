package com.civic.reporting.serviceImpl;

import java.util.Optional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.civic.reporting.model.Assignment;
import com.civic.reporting.model.Issue;
import com.civic.reporting.model.enumFolder.Category;
import com.civic.reporting.model.enumFolder.Status;
import com.civic.reporting.repository.AssignmentRepository;
import com.civic.reporting.repository.DepartmentRepository;
import com.civic.reporting.repository.IssueRepository;
import com.civic.reporting.service.ExifService;
import com.civic.reporting.service.VisionService;
import com.civic.reporting.service.VisionResult;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ClassificationWorker {

    private final IssueRepository issueRepo;
    private final DepartmentRepository departmentRepo;
    private final AssignmentRepository assignmentRepo;
    private final VisionService visionService;
    private final ExifService exifService;

    @Async
    @Transactional
    public void classify(Long issueId, String photoUrl, Double userLat, Double userLng) {
        Optional<Issue> oi = issueRepo.findById(issueId);
        if (oi.isEmpty())
            return;
        Issue issue = oi.get();

        // EXIF
        exifService.extractGps(photoUrl).ifPresent(gps -> {
            issue.setImageLatitude(gps[0]);
            issue.setImageLongitude(gps[1]);
            if (userLat != null && userLng != null) {
                double dist = haversine(userLat, userLng, gps[0], gps[1]);
                issue.setGpsDistanceMeters(dist);
            }
        });

        // Vision
        Optional<VisionResult> vr = visionService.analyzeImage(photoUrl);
        vr.ifPresent(r -> {
            issue.setVisionResponseJson(r.rawJson);
            issue.setPredictionConfidence(r.confidence);
            // safe map label to Category
            Category mapped = mapLabelToCategory(r.label);
            issue.setPredictedCategory(mapped);
            // simple authenticity heuristic: if web matches exist reduce score — for now
            // set to 80
            issue.setAuthenticityScore(80.0);
        });

        // Decide assignment
        if (issue.getPredictedCategory() != null && issue.getPredictionConfidence() != null
                && issue.getPredictionConfidence() >= 0.7) {
            issue.setCategory(issue.getPredictedCategory());
            issue.setStatus(Status.ASSIGNED);
            // map category -> department name (simple)
            String deptName = mapCategoryToDepartment(issue.getCategory());
            departmentRepo.findByName(deptName).ifPresent(dept -> {
                Assignment a = new Assignment();
                a.setIssue(issue);
                a.setAssignedAt(java.time.LocalDateTime.now());
                assignmentRepo.save(a);
            });
        } else {
            issue.setStatus(Status.MANUAL_REVIEW);
        }
        if (issue.getPredictedCategory() != null) {
            issue.setPriorityScore(computePriority(issue.getPredictedCategory(), issue.getPredictionConfidence()));
        }

        issueRepo.save(issue);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // metres
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private Category mapLabelToCategory(String label) {
        if (label == null)
            return null;
        String n = label.trim().toUpperCase().replaceAll("\\s+", "_");
        try {
            Category parsed = Category.valueOf(n);
            return isDbSafeCategory(parsed) ? parsed : null;
        } catch (IllegalArgumentException e) {
            // fallback mappings
            if (n.contains("TRASH") || n.contains("GARBAGE") || n.contains("LITTER"))
                return Category.GARBAGE;
            if (n.contains("POTHOLE") || n.contains("ROAD"))
                return Category.ROAD;
            if (n.contains("WATER") || n.contains("LEAK") || n.contains("PIPE"))
                return Category.WATER;
            if (n.contains("LIGHT") || n.contains("ELECTRIC"))
                return Category.ELECTRICITY;
            return null;
        }
    }

    private boolean isDbSafeCategory(Category category) {
        return category == Category.ROAD
                || category == Category.WATER
                || category == Category.GARBAGE
                || category == Category.ELECTRICITY;
    }

    private String mapCategoryToDepartment(Category c) {
        if (c == null)
            return "General";
        switch (c) {
            case WASTE:
                return "Sanitation";
            case ROAD:
                return "Public Works";
            default:
                return "General";
        }
    }

    private double computePriority(Category category, Double confidence) {
        double base;
        switch (category) {
            case ROAD:
            case WATER:
            case ELECTRICITY:
                base = 75.0;
                break;
            case WASTE:
            case GARBAGE:
                base = 65.0;
                break;
            default:
                base = 50.0;
                break;
        }
        if (confidence == null) {
            return base;
        }
        return Math.min(100.0, base + (confidence * 10.0));
    }
}
