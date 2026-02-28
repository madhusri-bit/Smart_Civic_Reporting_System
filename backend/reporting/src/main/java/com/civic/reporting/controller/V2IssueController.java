package com.civic.reporting.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.civic.reporting.dto.AssignDepartmentDTO;
import com.civic.reporting.dto.AssignOfficerDTO;
import com.civic.reporting.dto.CommentDTO;
import com.civic.reporting.dto.IssueSummaryDTO;
import com.civic.reporting.dto.ReviewDTO;
import com.civic.reporting.dto.IssueReportDTO;
import com.civic.reporting.model.*;
import com.civic.reporting.model.enumFolder.Category;
import com.civic.reporting.model.enumFolder.PhotoType;
import com.civic.reporting.model.enumFolder.Role;
import com.civic.reporting.model.enumFolder.Status;
import com.civic.reporting.model.enumFolder.WorkflowStatus;
import com.civic.reporting.repository.*;
import com.civic.reporting.service.*;
import com.civic.reporting.serviceImpl.RoleGuard;
import com.civic.reporting.serviceImpl.SeverityService;
import com.civic.reporting.serviceImpl.WorkflowService;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/issues")
@RequiredArgsConstructor
public class V2IssueController {

    private final IssueService issueService;
    private final IssueRepository issueRepo;
    private final IssueVoteRepository voteRepo;
    private final IssueCommentRepository commentRepo;
    private final IssuePhotoRepository photoRepo;
    private final IssueReporterRepository reporterRepo;
    private final IssueDepartmentRepository issueDeptRepo;
    private final AssignmentRepository assignmentRepo;
    private final IssueStatusHistoryRepository statusRepo;
    private final IssueResolutionRepository resolutionRepo;
    private final IssueReviewRepository reviewRepo;
    private final DepartmentRepository departmentRepo;
    private final UserRepository userRepo;

    private final S3Service s3Service;
    private final ExifService exifService;
    private final GeminiService geminiService;
    private final VisionService visionService;
    private final ImageCompareService imageCompareService;

    private final RoleGuard roleGuard;
    private final WorkflowService workflowService;
    private final SeverityService severityService;

    @PostMapping(path = "/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object reportWithPhoto(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestHeader(value = "Authorization", required = false) String auth) throws IOException {

        User reporter = roleGuard.requireRole(auth, Role.CITIZEN);

        if (photo == null || photo.isEmpty()) {
            throw new RuntimeException("Photo required");
        }

        String photoUrl = s3Service.uploadFile(photo);

        var exifOpt = exifService.extractGps(photoUrl);
        if (exifOpt.isEmpty()) {
            throw new RuntimeException("Photo must contain GPS metadata");
        }
        double[] gps = exifOpt.get();

        String finalTitle = title;
        String finalDescription = description;
        String categoryText = null;
        String visionLabel = null;

        var gem = geminiService.analyzeImage(photoUrl);
        if (gem.isPresent()) {
            var res = gem.get();
            if (finalTitle == null || finalTitle.isBlank()) {
                finalTitle = res.getTitle();
            }
            if (finalDescription == null || finalDescription.isBlank()) {
                finalDescription = res.getDescription();
            }
            categoryText = res.getCategory();
        }

        Category mappedCategory = mapCategory(categoryText);
        if (mappedCategory == null) {
            try {
                var vision = visionService.analyzeImage(photoUrl);
                if (vision.isPresent()) {
                    var vr = vision.get();
                    visionLabel = vr.label;
                    mappedCategory = mapCategory(vr.label);
                    if (finalDescription == null || finalDescription.isBlank()) {
                        finalDescription = "Detected via Vision: " + vr.label;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (mappedCategory == null) {
            mappedCategory = classifyCategoryFromText(finalTitle, finalDescription, categoryText, visionLabel);
        }

        for (Issue other : issueRepo.findAll()) {
            if (other.getImageLatitude() != null && other.getImageLongitude() != null) {
                double d = haversine(other.getImageLatitude(), other.getImageLongitude(), gps[0], gps[1]);
                if (d < 100) {
                    boolean categoryMatch = mappedCategory != null
                            && (mappedCategory.equals(other.getPredictedCategory())
                                    || mappedCategory.equals(other.getCategory()));
                    boolean titleMatch = isSimilarTitle(finalTitle, other.getTitle());
                    if (categoryMatch || titleMatch) {
                        return Map.of(
                                "status", "duplicate",
                                "message", "Issue already reported nearby. Please upvote instead.",
                                "existingIssueId", other.getId(),
                                "distanceMeters", d);
                    }
                }
            }
        }

        IssueReportDTO dto = new IssueReportDTO();
        dto.setTitle(finalTitle == null || finalTitle.isBlank() ? "Reported Issue" : finalTitle);
        dto.setDescription(finalDescription);
        dto.setLatitude(latitude != null ? latitude : gps[0]);
        dto.setLongitude(longitude != null ? longitude : gps[1]);
        dto.setPhotoUrl(photoUrl);

        Issue created = issueService.reportIssue(dto);
        applyPrediction(created, mappedCategory);
        created.setImageLatitude(gps[0]);
        created.setImageLongitude(gps[1]);
        created.setGpsDistanceMeters(0.0);
        created = issueRepo.save(created);

        IssueReporter link = new IssueReporter();
        link.setIssue(created);
        link.setReporter(reporter);
        link.setCreatedAt(LocalDateTime.now());
        reporterRepo.save(link);

        workflowService.add(created, WorkflowStatus.REPORTED, "Issue reported with photo");

        if (mappedCategory != null) {
            Department dept = matchDepartment(mappedCategory);
            if (dept != null) {
                IssueDepartment assign = new IssueDepartment();
                assign.setIssue(created);
                assign.setDepartment(dept);
                assign.setAssignedAt(LocalDateTime.now());
                issueDeptRepo.save(assign);
            }
        }

        return created;
    }

    @PostMapping("/{id}/upvote")
    public Object upvote(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        User user = roleGuard.requireUser(auth);
        Issue issue = issueRepo.findById(id).orElseThrow(() -> new RuntimeException("Issue not found"));

        boolean already = voteRepo.findAll().stream()
                .anyMatch(v -> v.getIssue() != null && v.getIssue().getId().equals(id)
                        && v.getUser() != null && v.getUser().getId().equals(user.getId()));
        if (already) {
            return Map.of("status", "already_upvoted");
        }

        IssueVote vote = new IssueVote();
        vote.setIssue(issue);
        vote.setUser(user);
        voteRepo.save(vote);

        return Map.of("status", "ok", "upvotes", severityService.countVotes(issue));
    }

    @PostMapping("/{id}/comment")
    public IssueComment comment(@PathVariable Long id,
            @RequestBody CommentDTO dto,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        User user = roleGuard.requireUser(auth);
        Issue issue = issueRepo.findById(id).orElseThrow(() -> new RuntimeException("Issue not found"));

        IssueComment comment = new IssueComment();
        comment.setIssue(issue);
        comment.setUser(user);
        comment.setContent(dto.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepo.save(comment);
    }

    @GetMapping("/{id}/timeline")
    public List<IssueStatusHistory> timeline(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        roleGuard.requireUser(auth);
        return statusRepo.findAll().stream()
                .filter(h -> h.getIssue() != null && h.getIssue().getId().equals(id))
                .sorted(Comparator.comparing(IssueStatusHistory::getCreatedAt))
                .collect(Collectors.toList());
    }

    @GetMapping("/my")
    public List<IssueSummaryDTO> myIssues(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        User user = roleGuard.requireUser(auth);

        List<Long> issueIds = reporterRepo.findAll().stream()
                .filter(r -> r.getReporter() != null && r.getReporter().getId().equals(user.getId()))
                .map(r -> r.getIssue().getId())
                .collect(Collectors.toList());

        return issueRepo.findAll().stream()
                .filter(i -> issueIds.contains(i.getId()))
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    private boolean isValidGps(Double lat, Double lng, double[] gps) {
        return lat != null && lng != null && gps != null && gps.length >= 2;
    }

    @PostMapping(path = "/{id}/resolve", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object resolveIssue(
            @PathVariable Long id,
            @RequestParam("photo") MultipartFile photo,
            @RequestHeader(value = "Authorization", required = false) String auth) throws IOException {
        roleGuard.requireRole(auth, Role.OFFICER, Role.HEAD);
        Issue issue = issueRepo.findById(id).orElseThrow(() -> new RuntimeException("Issue not found"));

        if (photo == null || photo.isEmpty()) {
            throw new RuntimeException("Photo required");
        }

        String photoUrl = s3Service.uploadFile(photo);
        Double ssim = imageCompareService.compareImages(issue.getPhotoUrl(), photoUrl);

        Double gpsDistance = null;
        var exifOpt = exifService.extractGps(photoUrl);
        if (exifOpt.isPresent()) {
            double[] gps = exifOpt.get();
            double baseLat = issue.getImageLatitude() != null ? issue.getImageLatitude() : issue.getLatitude();
            double baseLng = issue.getImageLongitude() != null ? issue.getImageLongitude() : issue.getLongitude();
            if (isValidGps(baseLat, baseLng, gps)) {
                gpsDistance = haversine(baseLat, baseLng, gps[0], gps[1]);
            }
        }

        String visionSummary = null;
        boolean labelOk = false;
        try {
            var vision = visionService.analyzeImage(photoUrl);
            if (vision.isPresent()) {
                var vr = vision.get();
                visionSummary = vr.label + " (" + vr.confidence + ")";
                String label = vr.label == null ? "" : vr.label.toLowerCase();
                labelOk = label.contains("clean") || label.contains("road") || label.contains("street")
                        || label.contains("park") || label.contains("sidewalk");
            }
        } catch (Exception ignored) {
        }

        boolean gpsOk = gpsDistance == null || gpsDistance <= 200;
        boolean ssimOk = ssim != null && ssim < 0.6;
        boolean aiResolved = gpsOk && (ssimOk || labelOk);

        IssuePhoto after = new IssuePhoto();
        after.setIssue(issue);
        after.setImageUrl(photoUrl);
        after.setType(PhotoType.AFTER);
        after.setSsimScore(ssim);
        photoRepo.save(after);

        IssueResolution resolution = new IssueResolution();
        resolution.setIssue(issue);
        resolution.setResolvedPhotoUrl(photoUrl);
        resolution.setSsimScore(ssim);
        resolution.setGpsDistanceMeters(gpsDistance);
        resolution.setVisionSummary(visionSummary);
        resolution.setAiResolved(aiResolved);
        resolution.setCreatedAt(LocalDateTime.now());
        resolutionRepo.save(resolution);

        issue.setStatus(Status.RESOLVED);
        issueRepo.save(issue);

        workflowService.add(issue, WorkflowStatus.RESOLVED, "Resolution submitted by department");
        workflowService.add(issue, WorkflowStatus.WAITING_REVIEW, "Waiting for citizen review");

        return Map.of("status", "waiting_review", "aiResolved", aiResolved, "ssim", ssim, "gpsDistance", gpsDistance);
    }

    @PostMapping("/{id}/review")
    public Object review(@PathVariable Long id,
            @RequestBody ReviewDTO dto,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        User user = roleGuard.requireUser(auth);
        Issue issue = issueRepo.findById(id).orElseThrow(() -> new RuntimeException("Issue not found"));

        IssueReview review = new IssueReview();
        review.setIssue(issue);
        review.setReviewer(user);
        review.setApproved(dto.isApproved());
        review.setComment(dto.getComment());
        review.setCreatedAt(LocalDateTime.now());
        reviewRepo.save(review);

        if (dto.isApproved()) {
            workflowService.add(issue, WorkflowStatus.CLOSED, "Citizen confirmed resolution");
        } else {
            workflowService.add(issue, WorkflowStatus.IN_PROGRESS, "Citizen rejected resolution");
        }

        return Map.of("status", dto.isApproved() ? "closed" : "reopened");
    }

    @GetMapping("/department/{departmentId}")
    public List<IssueSummaryDTO> departmentIssues(
            @PathVariable Long departmentId,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        roleGuard.requireRole(auth, Role.OFFICER, Role.HEAD);

        List<Long> issueIds = issueDeptRepo.findAll().stream()
                .filter(d -> d.getDepartment() != null && d.getDepartment().getId().equals(departmentId))
                .map(d -> d.getIssue().getId())
                .collect(Collectors.toList());

        return issueRepo.findAll().stream()
                .filter(i -> issueIds.contains(i.getId()))
                .sorted(Comparator.comparingDouble(severityService::computeSeverity).reversed())
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @PostMapping("/assign-department")
    public Object assignDepartment(
            @RequestBody AssignDepartmentDTO dto,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        roleGuard.requireRole(auth, Role.HEAD, Role.ADMIN);
        Issue issue = issueRepo.findById(dto.getIssueId()).orElseThrow(() -> new RuntimeException("Issue not found"));
        Department dept = departmentRepo.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        IssueDepartment assign = new IssueDepartment();
        assign.setIssue(issue);
        assign.setDepartment(dept);
        assign.setAssignedAt(LocalDateTime.now());
        issueDeptRepo.save(assign);

        workflowService.add(issue, WorkflowStatus.IN_PROGRESS, "Assigned to department " + dept.getName());
        return Map.of("status", "assigned");
    }

    @PostMapping("/assign-officer")
    public Object assignOfficer(
            @RequestBody AssignOfficerDTO dto,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        roleGuard.requireRole(auth, Role.HEAD);
        Issue issue = issueRepo.findById(dto.getIssueId()).orElseThrow(() -> new RuntimeException("Issue not found"));
        User officer = userRepo.findById(dto.getOfficerId()).orElseThrow(() -> new RuntimeException("User not found"));

        Assignment assignment = new Assignment();
        assignment.setIssue(issue);
        assignment.setAssignedTo(officer);
        assignment.setAssignedAt(LocalDateTime.now());
        assignmentRepo.save(assignment);

        workflowService.add(issue, WorkflowStatus.IN_PROGRESS, "Assigned to officer " + officer.getName());
        return Map.of("status", "assigned");
    }

    private IssueSummaryDTO toSummary(Issue issue) {
        IssueSummaryDTO dto = new IssueSummaryDTO();
        dto.setId(issue.getId());
        dto.setTitle(issue.getTitle());
        dto.setDescription(issue.getDescription());
        dto.setPhotoUrl(issue.getPhotoUrl());
        dto.setCategory(issue.getCategory() == null ? null : issue.getCategory().name());
        dto.setStatus(issue.getStatus() == null ? null : issue.getStatus().name());
        dto.setUpvotes(severityService.countVotes(issue));
        dto.setComments(severityService.countComments(issue));
        dto.setSeverity(severityService.computeSeverity(issue));
        return dto;
    }

    private Category mapCategory(String categoryText) {
        if (categoryText == null || categoryText.isBlank()) {
            return null;
        }
        String normalized = categoryText.trim().toUpperCase().replaceAll("\\s+", "_");
        try {
            Category parsed = Category.valueOf(normalized);
            return isDbSafeCategory(parsed) ? parsed : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void applyPrediction(Issue issue, Category mapped) {
        if (mapped == null) {
            return;
        }
        issue.setPredictedCategory(mapped);
        issue.setCategory(mapped);
        issue.setPredictionConfidence(0.80);
        issue.setPriorityScore(defaultPriority(mapped));
    }

    private boolean isDbSafeCategory(Category category) {
        return category == Category.ROAD_INFRA
                || category == Category.WATER_SUPPLY
                || category == Category.SOLID_WASTE
                || category == Category.ELECTRICITY
                || category == Category.PARKS
                || category == Category.PUBLIC_SAFETY
                || category == Category.URBAN_PLANNING
                || category == Category.ANIMAL_CONTROL
                || category == Category.PUBLIC_HEALTH
                || category == Category.OTHER
                || category == Category.ROAD
                || category == Category.WATER
                || category == Category.GARBAGE
                || category == Category.WASTE
                || category == Category.GENERAL;
    }

    private Category classifyCategoryFromText(String title, String description, String categoryText, String visionLabel) {
        String combined = String.join(" ",
                safeLower(title),
                safeLower(description),
                safeLower(categoryText),
                safeLower(visionLabel));

        if (matchAny(combined, "pothole", "damaged road", "broken footpath", "streetlight",
                "traffic signal", "road crack", "manhole", "road sign", "public works", "pwd")) {
            return Category.ROAD_INFRA;
        }
        if (matchAny(combined, "water leakage", "pipe burst", "no water", "low water pressure",
                "drainage", "sewage", "open drainage", "contaminated water", "water supply")) {
            return Category.WATER_SUPPLY;
        }
        if (matchAny(combined, "garbage", "trash", "dustbin", "illegal dumping", "dead animal",
                "medical waste", "construction debris", "street sweeping", "litter")) {
            return Category.SOLID_WASTE;
        }
        if (matchAny(combined, "power outage", "electric pole", "wire sparking", "transformer",
                "voltage", "loose wire", "meter", "electricity")) {
            return Category.ELECTRICITY;
        }
        if (matchAny(combined, "park", "playground", "bench", "fallen tree", "overgrown grass",
                "gym equipment", "water fountain", "public seating")) {
            return Category.PARKS;
        }
        if (matchAny(combined, "illegal parking", "encroachment", "noise pollution",
                "public nuisance", "unauthorized vendor", "suspicious", "traffic violation")) {
            return Category.PUBLIC_SAFETY;
        }
        if (matchAny(combined, "illegal construction", "dangerous building", "permit",
                "structural damage", "zoning violation", "urban planning")) {
            return Category.URBAN_PLANNING;
        }
        if (matchAny(combined, "stray dog", "injured animal", "cattle on road",
                "animal attack", "rabies", "animal shelter")) {
            return Category.ANIMAL_CONTROL;
        }
        if (matchAny(combined, "mosquito", "fogging", "public toilet", "food safety",
                "disease outbreak", "health", "water contamination")) {
            return Category.PUBLIC_HEALTH;
        }
        return Category.OTHER;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private boolean matchAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSimilarTitle(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        String left = a.trim().toLowerCase();
        String right = b.trim().toLowerCase();
        if (left.isBlank() || right.isBlank()) {
            return false;
        }
        return left.contains(right) || right.contains(left);
    }

    private double defaultPriority(Category category) {
        switch (category) {
            case ROAD_INFRA:
            case WATER_SUPPLY:
            case ELECTRICITY:
            case PUBLIC_SAFETY:
            case URBAN_PLANNING:
                return 80.0;
            case SOLID_WASTE:
            case PUBLIC_HEALTH:
            case ANIMAL_CONTROL:
                return 70.0;
            case PARKS:
                return 60.0;
            case ROAD:
            case WATER:
            case GARBAGE:
                return 70.0;
            default:
                return 50.0;
        }
    }

    private Department matchDepartment(Category category) {
        if (category == null) {
            return null;
        }
        String name = null;
        switch (category) {
            case ROAD_INFRA:
                name = "Road & Infrastructure Department";
                break;
            case WATER_SUPPLY:
                name = "Water Supply & Sanitation Department";
                break;
            case SOLID_WASTE:
                name = "Solid Waste Management Department";
                break;
            case ELECTRICITY:
                name = "Electricity Board";
                break;
            case PARKS:
                name = "Parks & Recreation Department";
                break;
            case PUBLIC_SAFETY:
                name = "Police / Public Safety Department";
                break;
            case URBAN_PLANNING:
                name = "Urban Planning & Development Authority";
                break;
            case ANIMAL_CONTROL:
                name = "Animal Control Department";
                break;
            case PUBLIC_HEALTH:
                name = "Public Health Department";
                break;
            default:
                name = null;
        }
        if (name == null) {
            return null;
        }
        String target = name.toLowerCase();
        return departmentRepo.findAll().stream()
                .filter(d -> d.getName() != null && d.getName().toLowerCase().equals(target))
                .findFirst()
                .orElseGet(() -> departmentRepo.findAll().stream()
                        .filter(d -> d.getName() != null && d.getName().toLowerCase().contains(target))
                        .findFirst()
                        .orElse(null));
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
