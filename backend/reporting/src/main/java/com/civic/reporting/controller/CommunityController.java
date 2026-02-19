package com.civic.reporting.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.civic.reporting.model.CommunityPost;
import com.civic.reporting.repository.CommunityRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityRepository repo;

    @PostMapping
    public CommunityPost create(@RequestBody CommunityPost post) {
        return repo.save(post);
    }
}
