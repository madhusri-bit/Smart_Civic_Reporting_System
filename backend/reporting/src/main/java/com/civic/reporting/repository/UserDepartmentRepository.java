package com.civic.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.civic.reporting.model.UserDepartment;

public interface UserDepartmentRepository extends JpaRepository<UserDepartment, Long> {
}
