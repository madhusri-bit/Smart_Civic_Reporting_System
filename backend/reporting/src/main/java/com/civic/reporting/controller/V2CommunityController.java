package com.civic.reporting.controller;

import org.springframework.web.bind.annotation.*;

import com.civic.reporting.dto.CommunityPostDTO;
import com.civic.reporting.dto.IssueSummaryDTO;
import com.civic.reporting.model.CommunityIssuePost;
import com.civic.reporting.model.Issue;
import com.civic.reporting.model.IssueVote;
import com.civic.reporting.model.enumFolder.Role;
import com.civic.reporting.repository.CommunityIssuePostRepository;
import com.civic.reporting.repository.IssueRepository;
import com.civic.reporting.repository.IssueVoteRepository;
import com.civic.reporting.serviceImpl.RoleGuard;
import com.civic.reporting.serviceImpl.SeverityService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/community")
@RequiredArgsConstructor
public class V2CommunityController {

    private final CommunityIssuePostRepository postRepo;
    private final IssueRepository issueRepo;
    private final IssueVoteRepository voteRepo;
    private final RoleGuard roleGuard;
    private final SeverityService severityService;

    @PostMapping("/posts")
    public CommunityIssuePost createPost(
            @RequestBody CommunityPostDTO dto,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        var user = roleGuard.requireUser(auth);
        Issue issue = issueRepo.findById(dto.getIssueId()).orElseThrow(() -> new RuntimeException("Issue not found"));

        CommunityIssuePost post = new CommunityIssuePost();
        post.setIssue(issue);
        post.setCreatedBy(user);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCreatedAt(LocalDateTime.now());
        return postRepo.save(post);
    }

    @GetMapping("/posts")
    public List<Map<String, Object>> listPosts(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        roleGuard.requireRole(auth, Role.CITIZEN, Role.OFFICER, Role.HEAD, Role.ADMIN);

        return postRepo.findAll().stream()
                .sorted(Comparator.comparing(CommunityIssuePost::getCreatedAt).reversed())
                .map(p -> {
                    Issue issue = p.getIssue();
                    IssueSummaryDTO summary = new IssueSummaryDTO();
                    summary.setId(issue.getId());
                    summary.setTitle(issue.getTitle());
                    summary.setDescription(issue.getDescription());
                    summary.setPhotoUrl(issue.getPhotoUrl());
                    summary.setCategory(issue.getCategory() == null ? null : issue.getCategory().name());
                    summary.setStatus(issue.getStatus() == null ? null : issue.getStatus().name());
                    summary.setUpvotes(severityService.countVotes(issue));
                    summary.setComments(severityService.countComments(issue));
                    summary.setSeverity(severityService.computeSeverity(issue));

                    Map<String, Object> row = new HashMap<>();
                    row.put("postId", p.getId());
                    row.put("title", p.getTitle());
                    row.put("content", p.getContent());
                    row.put("createdAt", p.getCreatedAt());
                    row.put("issue", summary);
                    return row;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/issues")
    public List<IssueSummaryDTO> nearbyIssues(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng,
            @RequestParam(value = "km", defaultValue = "2") double km,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        roleGuard.requireRole(auth, Role.CITIZEN, Role.OFFICER, Role.HEAD, Role.ADMIN);

        double maxMeters = km * 1000.0;
        return issueRepo.findAll().stream()
                .filter(i -> i.getLatitude() != null && i.getLongitude() != null)
                .filter(i -> haversine(lat, lng, i.getLatitude(), i.getLongitude()) <= maxMeters)
                .sorted(Comparator.comparingDouble(severityService::computeSeverity).reversed())
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @GetMapping("/upvoted")
    public List<IssueSummaryDTO> myUpvoted(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        var user = roleGuard.requireUser(auth);
        List<Long> issueIds = voteRepo.findAll().stream()
                .filter(v -> v.getUser() != null && v.getUser().getId().equals(user.getId()))
                .map(IssueVote::getIssue)
                .filter(Objects::nonNull)
                .map(Issue::getId)
                .distinct()
                .collect(Collectors.toList());

        return issueRepo.findAll().stream()
                .filter(i -> issueIds.contains(i.getId()))
                .sorted(Comparator.comparingDouble(severityService::computeSeverity).reversed())
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    private IssueSummaryDTO toSummary(Issue issue) {
        IssueSummaryDTO summary = new IssueSummaryDTO();
        summary.setId(issue.getId());
        summary.setTitle(issue.getTitle());
        summary.setDescription(issue.getDescription());
        summary.setPhotoUrl(issue.getPhotoUrl());
        summary.setCategory(issue.getCategory() == null ? null : issue.getCategory().name());
        summary.setStatus(issue.getStatus() == null ? null : issue.getStatus().name());
        summary.setUpvotes(severityService.countVotes(issue));
        summary.setComments(severityService.countComments(issue));
        summary.setSeverity(severityService.computeSeverity(issue));
        return summary;
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
