package com.civic.reporting.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.civic.reporting.model.Location;
import com.civic.reporting.model.User;
import com.civic.reporting.repository.UserRepository;
import com.civic.reporting.service.LocationService;
import com.civic.reporting.config.JwtUtil;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locService;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;

    @PostMapping
    public Location saveLocation(@RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> body) {
        String email = null;
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            email = jwtUtil.extractEmail(token);
        }

        User user = null;
        if (email != null) {
            user = userRepo.findByEmail(email);
        } else if (body.containsKey("userId")) {
            Long uid = Long.valueOf(body.get("userId").toString());
            user = userRepo.findById(uid).orElse(null);
        }

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Double lat = Double.valueOf(body.get("latitude").toString());
        Double lng = Double.valueOf(body.get("longitude").toString());

        return locService.saveLocation(user, lat, lng);
    }
}
