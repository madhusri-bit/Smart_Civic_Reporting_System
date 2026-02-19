package com.civic.reporting.service;

import java.util.Optional;

public interface VisionService {

    /**
     * Analyze an image at the given URI (HTTP/S or GCS/S3 url) and return a simple
     * prediction.
     */
    Optional<VisionResult> analyzeImage(String imageUri);

}
