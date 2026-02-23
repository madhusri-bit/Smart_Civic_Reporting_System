package com.civic.reporting.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.civic.reporting.model.Location;
import com.civic.reporting.model.User;
import com.civic.reporting.repository.LocationRepository;
import com.civic.reporting.service.LocationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locRepo;

    @Override
    public Location saveLocation(User user, Double lat, Double lng) {
        Location l = new Location();
        l.setUser(user);
        l.setLatitude(lat);
        l.setLongitude(lng);
        l.setCapturedAt(LocalDateTime.now());
        return locRepo.save(l);
    }

    @Override
    public Location getLatestLocation(User user) {
        List<Location> list = locRepo.findByUserOrderByCapturedAtDesc(user);
        if (list.isEmpty())
            return null;
        return list.get(0);
    }
}
