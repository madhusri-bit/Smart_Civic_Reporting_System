package com.civic.reporting.controller;

import org.springframework.web.bind.annotation.*;

import com.civic.reporting.model.Department;
import com.civic.reporting.model.User;
import com.civic.reporting.model.UserDepartment;
import com.civic.reporting.model.enumFolder.Role;
import com.civic.reporting.repository.DepartmentRepository;
import com.civic.reporting.repository.IssueRepository;
import com.civic.reporting.repository.UserDepartmentRepository;
import com.civic.reporting.repository.UserRepository;
import com.civic.reporting.serviceImpl.RoleGuard;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v2/admin")
@RequiredArgsConstructor
public class V2AdminController {

    private final RoleGuard roleGuard;
    private final DepartmentRepository departmentRepo;
    private final UserRepository userRepo;
    private final IssueRepository issueRepo;
    private final UserDepartmentRepository userDeptRepo;

    @GetMapping("/departments")
    public List<Department> departments(@RequestHeader(value = "Authorization", required = false) String auth) {
        roleGuard.requireRole(auth, Role.ADMIN);
        return departmentRepo.findAll();
    }

    @GetMapping("/users")
    public List<User> users(@RequestHeader(value = "Authorization", required = false) String auth) {
        roleGuard.requireRole(auth, Role.ADMIN);
        return userRepo.findAll();
    }

    @GetMapping("/issues")
    public Object issues(@RequestHeader(value = "Authorization", required = false) String auth) {
        roleGuard.requireRole(auth, Role.ADMIN);
        return issueRepo.findAll();
    }

    @PostMapping("/departments/{departmentId}/users/{userId}")
    public Object assignUserToDepartment(
            @PathVariable Long departmentId,
            @PathVariable Long userId,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        roleGuard.requireRole(auth, Role.ADMIN);
        Department dept = departmentRepo.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        UserDepartment link = new UserDepartment();
        link.setDepartment(dept);
        link.setUser(user);
        link.setAssignedAt(LocalDateTime.now());
        userDeptRepo.save(link);

        return java.util.Map.of("status", "assigned");
    }
}
