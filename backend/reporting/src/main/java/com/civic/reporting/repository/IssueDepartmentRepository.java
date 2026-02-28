package com.civic.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.civic.reporting.model.IssueDepartment;

public interface IssueDepartmentRepository extends JpaRepository<IssueDepartment, Long> {
}
