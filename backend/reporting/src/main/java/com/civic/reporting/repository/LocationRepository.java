package com.civic.reporting.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.civic.reporting.model.Location;
import com.civic.reporting.model.User;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByUserOrderByCapturedAtDesc(User user);
}
