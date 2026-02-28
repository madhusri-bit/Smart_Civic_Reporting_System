package com.civic.reporting.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.civic.reporting.model.User;
import com.civic.reporting.serviceImpl.RoleGuard;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/users")
@RequiredArgsConstructor
public class V2UserController {

    private final RoleGuard roleGuard;

    @GetMapping("/me")
    public Map<String, Object> me(@RequestHeader(value = "Authorization", required = false) String auth) {
        User user = roleGuard.requireUser(auth);
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("id", user.getId());
        payload.put("name", user.getName());
        payload.put("email", user.getEmail());
        payload.put("role", user.getRole() == null ? null : user.getRole().name());
        payload.put("city", user.getCity());
        payload.put("state", user.getState());
        payload.put("pincode", user.getPincode());
        payload.put("address", user.getAddress());
        payload.put("profile", user.getProfile());
        return payload;
    }
}
