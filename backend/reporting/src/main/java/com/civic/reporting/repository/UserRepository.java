package com.civic.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.civic.reporting.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    public User findByEmail(String email);
}