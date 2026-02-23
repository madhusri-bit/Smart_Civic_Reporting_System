package com.civic.reporting.controller;

import lombok.*;
import org.springframework.web.bind.annotation.*;

import com.civic.reporting.config.JwtUtil;
import com.civic.reporting.model.User;
import com.civic.reporting.repository.UserRepository;
import com.civic.reporting.dto.RegisterDTO;
import com.civic.reporting.model.enumFolder.Role;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        User dbUser = userRepo.findByEmail(user.getEmail());
        if (dbUser == null) {
            throw new RuntimeException("Invalid credentials");
        }
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder enc = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        if (!enc.matches(user.getPassword(), dbUser.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return jwtUtil.generateToken(dbUser.getEmail());
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterDTO dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }
        User existing = userRepo.findByEmail(dto.getEmail());
        if (existing != null) {
            throw new RuntimeException("User already exists");
        }

        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder enc = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(enc.encode(dto.getPassword()));
        user.setPreferredLanguage(dto.getPreferredLanguage());
        user.setState(dto.getState());
        user.setCity(dto.getCity());
        user.setPincode(dto.getPincode());
        user.setAddress(dto.getAddress());
        user.setProfile(dto.getProfile());

        // set role if provided and valid, otherwise default to CITIZEN
        try {
            if (dto.getRole() != null && !dto.getRole().isBlank()) {
                user.setRole(Role.valueOf(dto.getRole()));
            } else {
                user.setRole(Role.CITIZEN);
            }
        } catch (Exception e) {
            user.setRole(Role.CITIZEN);
        }

        user.setCreatedAt(java.time.LocalDateTime.now());
        User saved = userRepo.save(user);
        return jwtUtil.generateToken(saved.getEmail());
    }
}
