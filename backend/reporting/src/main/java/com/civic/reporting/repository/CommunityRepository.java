package com.civic.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.civic.reporting.model.CommunityPost;

public interface CommunityRepository extends JpaRepository<CommunityPost, Long> {

}
