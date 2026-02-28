package com.civic.reporting.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.civic.reporting.dto.AdminBootstrapDTO;
import com.civic.reporting.dto.DepartmentUserDTO;
import com.civic.reporting.model.Department;
import com.civic.reporting.model.User;
import com.civic.reporting.model.UserDepartment;
import com.civic.reporting.model.enumFolder.Role;
import com.civic.reporting.repository.DepartmentRepository;
import com.civic.reporting.repository.UserDepartmentRepository;
import com.civic.reporting.repository.UserRepository;
import com.civic.reporting.serviceImpl.RoleGuard;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/admin")
@RequiredArgsConstructor
public class V2AdminAuthController {

    private final RoleGuard roleGuard;
    private final UserRepository userRepo;
    private final DepartmentRepository departmentRepo;
    private final UserDepartmentRepository userDeptRepo;
    private final BCryptPasswordEncoder encoder;

    @PostMapping("/bootstrap")
    public Map<String, Object> bootstrapAdmin(@RequestBody AdminBootstrapDTO dto) {
        boolean hasAdmin = userRepo.findAll().stream()
                .anyMatch(u -> u.getRole() == Role.ADMIN);
        if (hasAdmin) {
            throw new RuntimeException("Admin already exists");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }
        if (userRepo.findByEmail(dto.getEmail()) != null) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();
        user.setName(dto.getName() == null || dto.getName().isBlank() ? "Admin" : dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRole(Role.ADMIN);
        user.setCreatedAt(LocalDateTime.now());
        User saved = userRepo.save(user);

        return Map.of("status", "created", "userId", saved.getId());
    }

    @PostMapping("/department-users")
    public Map<String, Object> createDepartmentUser(
            @RequestBody DepartmentUserDTO dto,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        roleGuard.requireRole(auth, Role.ADMIN);

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }
        if (userRepo.findByEmail(dto.getEmail()) != null) {
            throw new RuntimeException("User already exists");
        }

        Role role = Role.OFFICER;
        try {
            if (dto.getRole() != null) {
                role = Role.valueOf(dto.getRole());
            }
        } catch (Exception ignored) {
            role = Role.OFFICER;
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        User saved = userRepo.save(user);

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepo.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            UserDepartment link = new UserDepartment();
            link.setUser(saved);
            link.setDepartment(dept);
            link.setAssignedAt(LocalDateTime.now());
            userDeptRepo.save(link);
        }

        return Map.of("status", "created", "userId", saved.getId());
    }
}
