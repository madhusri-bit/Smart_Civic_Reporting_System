package com.civic.reporting.controller;

import lombok.*;
import org.springframework.web.bind.annotation.*;

import com.civic.reporting.config.JwtUtil;
import com.civic.reporting.model.User;
import com.civic.reporting.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        User dbUser = userRepo.findByEmail(user.getEmail());
        return jwtUtil.generateToken(dbUser.getEmail());
    }
}
