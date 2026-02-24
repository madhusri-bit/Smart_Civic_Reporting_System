package com.civic.reporting.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.civic.reporting.dto.IssueReportDTO;
import com.civic.reporting.model.Issue;
import com.civic.reporting.model.enumFolder.Category;
import com.civic.reporting.service.IssueService;
import com.civic.reporting.service.S3Service;
import com.civic.reporting.config.JwtUtil;
import com.civic.reporting.repository.UserRepository;
import com.civic.reporting.model.User;
import com.civic.reporting.service.ExifService;
import com.civic.reporting.service.LocationService;
import com.civic.reporting.service.GeminiService;
import com.civic.reporting.service.GeminiResult;
import com.civic.reporting.service.VisionResult;
import com.civic.reporting.service.VisionService;
import com.civic.reporting.repository.IssueRepository;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;
    private final S3Service s3Service;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;
    private final ExifService exifService;
    private final LocationService locationService;
    private final GeminiService geminiService;
    private final VisionService visionService;
    private final IssueRepository issueRepo;

    // Existing JSON endpoint (unchanged)
    @PostMapping("/report")
    public Issue reportIssue(@RequestBody IssueReportDTO dto) {
        return issueService.reportIssue(dto);
    }

    // Multipart endpoint for image upload (accepts form fields + photo file)
    @PostMapping(path = "/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Issue reportIssueMultipart(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "photo", required = false) MultipartFile photo) throws IOException {

        String photoUrl = null;
        if (photo != null && !photo.isEmpty()) {
            photoUrl = s3Service.uploadFile(photo);
        }

        IssueReportDTO dto = new IssueReportDTO();
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setLatitude(latitude);
        dto.setLongitude(longitude);
        dto.setPhotoUrl(photoUrl);

        return issueService.reportIssue(dto);
    }

    // Image-only upload flow: accepts only photo, identifies GPS from EXIF,
    // compares
    // with user's latest location, checks duplication, then creates issue.
    @PostMapping(path = "/report-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object reportImageOnly(@RequestParam("photo") MultipartFile photo,
            @RequestHeader(value = "Authorization", required = false) String auth) throws Exception {

        if (photo == null || photo.isEmpty()) {
            throw new RuntimeException("Photo required");
        }

        // Upload to S3
        String photoUrl = s3Service.uploadFile(photo);

        // Extract EXIF GPS
        var exifOpt = exifService.extractGps(photoUrl);
        if (exifOpt.isEmpty()) {
            return Map.of("status", "error", "message", "No GPS metadata found in image");
        }
        double[] gps = exifOpt.get();

        // Identify user from token (optional)
        User user = null;
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            String email = jwtUtil.extractEmail(token);
            user = userRepo.findByEmail(email);
        }

        // If user present, compare with latest saved location
        if (user != null) {
            var loc = locationService.getLatestLocation(user);
            if (loc == null) {
                return Map.of("status", "error", "message", "No saved user location to compare");
            }
            double userLat = loc.getLatitude();
            double userLng = loc.getLongitude();
            double dist = haversine(userLat, userLng, gps[0], gps[1]);
            if (dist > 500) { // threshold 500 meters
                return Map.of("status", "error", "message", "Photo GPS does not match user location", "distanceMeters",
                        dist);
            }
        }

        // Run Gemini/vision to synthesize title/category
        var gem = geminiService.analyzeImage(photoUrl);
        String title = "Reported Issue";
        String description = null;
        String categoryText = null;
        if (gem.isPresent()) {
            GeminiResult res = gem.get();
            if (res.getTitle() != null && !res.getTitle().isBlank()) {
                title = res.getTitle();
            }
            description = res.getDescription();
            categoryText = res.getCategory();
        }
        Category mappedCategory = mapCategory(categoryText);
        if (mappedCategory == null) {
            try {
                var vision = visionService.analyzeImage(photoUrl);
                if (vision.isPresent()) {
                    VisionResult vr = vision.get();
                    mappedCategory = mapCategory(vr.label);
                    if (description == null || description.isBlank()) {
                        description = "Detected via Vision: " + vr.label;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Duplicate check: naive—same category and nearby existing issue with similar
        // title
        var all = issueRepo.findAll();
        for (Issue other : all) {
            if (other.getImageLatitude() != null && other.getImageLongitude() != null) {
                double d = haversine(other.getImageLatitude(), other.getImageLongitude(), gps[0], gps[1]);
                if (d < 100) {
                    boolean categoryMatch = mappedCategory == null
                            || mappedCategory.equals(other.getPredictedCategory())
                            || mappedCategory.equals(other.getCategory());
                    if (categoryMatch) {
                        return Map.of(
                                "status", "duplicate",
                                "message", "Issue already reported nearby. Do you want to upvote?",
                                "action", "UPVOTE",
                                "existingIssueId", other.getId(),
                                "distanceMeters", d);
                    }
                }
            }
        }

        // Not duplicate — create Issue
        IssueReportDTO dto = new IssueReportDTO();
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setLatitude(gps[0]);
        dto.setLongitude(gps[1]);
        dto.setPhotoUrl(photoUrl);

        Issue created = issueService.reportIssue(dto);
        applyGeminiPrediction(created, mappedCategory);
        created = issueRepo.save(created);

        return created;
    }

    private void applyGeminiPrediction(Issue issue, Category mapped) {
        if (mapped == null) {
            return;
        }
        issue.setPredictedCategory(mapped);
        issue.setCategory(mapped);
        issue.setPredictionConfidence(0.80);
        issue.setPriorityScore(defaultPriority(mapped));
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
            if (normalized.contains("GARBAGE") || normalized.contains("TRASH") || normalized.contains("LITTER")) {
                return Category.GARBAGE;
            }
            return null;
        }
    }

    private boolean isDbSafeCategory(Category category) {
        return category == Category.ROAD
                || category == Category.WATER
                || category == Category.GARBAGE
                || category == Category.ELECTRICITY;
    }

    private double defaultPriority(Category category) {
        switch (category) {
            case ROAD:
            case WATER:
            case ELECTRICITY:
                return 75.0;
            case WASTE:
            case GARBAGE:
                return 65.0;
            default:
                return 50.0;
        }
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // metres
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
