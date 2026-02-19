package com.civic.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.civic.reporting.model.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    java.util.Optional<Department> findByName(String name);
}