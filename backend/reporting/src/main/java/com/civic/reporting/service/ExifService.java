package com.civic.reporting.service;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;

@Service
public class ExifService {

    public Optional<double[]> extractGps(String imageUrl) {
        try (InputStream in = new URL(imageUrl).openStream()) {
            Metadata metadata = ImageMetadataReader.readMetadata(in);
            GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gps != null && gps.getGeoLocation() != null) {
                double lat = gps.getGeoLocation().getLatitude();
                double lon = gps.getGeoLocation().getLongitude();
                return Optional.of(new double[] { lat, lon });
            }
        } catch (Exception e) {
            // ignore and return empty
        }
        return Optional.empty();
    }
}
