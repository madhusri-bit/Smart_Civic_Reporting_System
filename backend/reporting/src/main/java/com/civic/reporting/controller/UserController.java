package com.civic.reporting.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.civic.reporting.model.User;
import com.civic.reporting.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepo;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        if (userRepo.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Email already registered");
        }

        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder enc = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        user.setPassword(enc.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        return userRepo.save(user);
    }
}
