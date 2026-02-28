package com.civic.reporting.serviceImpl;

import org.springframework.stereotype.Service;
import com.civic.reporting.config.JwtUtil;
import com.civic.reporting.model.User;
import com.civic.reporting.model.enumFolder.Role;
import com.civic.reporting.repository.UserRepository;

@Service
public class RoleGuard {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;

    public RoleGuard(JwtUtil jwtUtil, UserRepository userRepo) {
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
    }

    public User requireUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization required");
        }
        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    public User requireRole(String authHeader, Role... roles) {
        User user = requireUser(authHeader);
        for (Role role : roles) {
            if (role == user.getRole()) {
                return user;
            }
        }
        throw new RuntimeException("Forbidden");
    }
}
