package com.civic.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.civic.reporting.model.IssuePhoto;

public interface IssuePhotoRepository extends JpaRepository<IssuePhoto, Long> {
}
