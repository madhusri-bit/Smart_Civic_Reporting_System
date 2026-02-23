package com.civic.reporting.service;

import com.civic.reporting.model.Location;
import com.civic.reporting.model.User;

public interface LocationService {
    Location saveLocation(User user, Double lat, Double lng);

    Location getLatestLocation(User user);
}
