package com.civic.reporting.service;

import java.util.Optional;

/**
 * Service for analyzing images using the Gemini model.
 */
public interface GeminiService {

    /**
     * Analyze the image located at {@code imageUrl} and return structured fields.
     *
     * @param imageUrl remote URL of the image to analyze
     * @return Optional containing a {@link GeminiResult} when analysis succeeds
     */
    Optional<GeminiResult> analyzeImage(String imageUrl);
}
// package com.civic.reporting.service;

// import java.util.Map;
// import java.util.Optional;

// public interface GeminiService {
// Optional<Map<String, String>> analyzeImage(String imageUrl);
// }
