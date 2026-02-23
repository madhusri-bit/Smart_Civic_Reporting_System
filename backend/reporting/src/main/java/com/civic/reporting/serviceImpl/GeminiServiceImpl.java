package com.civic.reporting.serviceImpl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.civic.reporting.service.GeminiResult;
import com.civic.reporting.service.GeminiService;
import com.civic.reporting.service.VisionResult;
import com.civic.reporting.service.VisionService;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiServiceImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final VisionService visionService;

    @Value("${gemini.api.url:https://api.example.com/v1/gemini}")
    private String geminiApiUrl;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    public Optional<GeminiResult> analyzeImage(String imageUrl) {
        try {
            byte[] imageBytes = downloadImage(imageUrl);
            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("Downloaded image is empty: {}", imageUrl);
                return Optional.empty();
            }

            String base64 = Base64.getEncoder().encodeToString(imageBytes);

            // Build a compact request payload. The target Gemini endpoint can be configured
            // via application.properties. The payload format is intentionally generic so it
            // can be adapted to whichever gateway or wrapper you use in production.
            var payload = new java.util.HashMap<String, Object>();
            payload.put("model", geminiModel);
            var input = new java.util.HashMap<String, Object>();
            input.put("instructions",
                    "Return only a single JSON object with fields: title, description, category (one of ROAD/WATER/GARBAGE/ELECTRICITY/GENERAL), department.");
            input.put("image_base64", base64);
            payload.put("input", input);

            String body = OBJECT_MAPPER.writeValueAsString(payload);

            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(geminiApiUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "civic-reporting/1.0")
                    .POST(BodyPublishers.ofString(body));

            if (geminiApiKey != null && !geminiApiKey.isBlank()) {
                reqBuilder.header("Authorization", "Bearer " + geminiApiKey);
            }

            HttpRequest request = reqBuilder.build();
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            int status = response.statusCode();
            String respBody = response.body();
            if (status < 200 || status >= 300) {
                log.warn("Gemini API returned non-2xx status: {} body={}", status, respBody);
                return Optional.empty();
            }

            // Try to parse the response directly into GeminiResult. Some Gemini
            // integrations
            // return plain JSON, others return text containing JSON. We attempt the common
            // cases to be resilient.
            try {
                GeminiResult direct = OBJECT_MAPPER.readValue(respBody, GeminiResult.class);
                return Optional.ofNullable(direct);
            } catch (Exception e) {
                // attempt to extract first JSON object from response text
                int start = respBody.indexOf('{');
                int end = respBody.lastIndexOf('}');
                if (start >= 0 && end > start) {
                    String json = respBody.substring(start, end + 1);
                    try {
                        GeminiResult parsed = OBJECT_MAPPER.readValue(json, GeminiResult.class);
                        return Optional.ofNullable(parsed);
                    } catch (Exception ex) {
                        log.debug("Failed parsing extracted JSON from Gemini response", ex);
                    }
                }
                log.warn("Unable to parse Gemini response into GeminiResult: {}", respBody);
            }

        } catch (IOException | InterruptedException ex) {
            log.error("Error calling Gemini API for image {}", imageUrl, ex);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("Unexpected error analyzing image {}", imageUrl, ex);
        }

        // Fallback: attempt to use VisionService if available to produce basic fields.
        try {
            Optional<VisionResult> vr = visionService.analyzeImage(imageUrl);
            if (vr.isPresent()) {
                VisionResult v = vr.get();
                GeminiResult fallback = new GeminiResult();
                String label = v.label != null ? v.label : "GENERAL";
                fallback.setTitle("Report: " + label);
                fallback.setDescription("Auto-generated description: " + label);
                fallback.setCategory(label);
                fallback.setDepartment("General");
                return Optional.of(fallback);
            }
        } catch (Exception e) {
            log.debug("VisionService fallback failed", e);
        }

        return Optional.empty();
    }

    private byte[] downloadImage(String imageUrl) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "civic-reporting-image-fetcher/1.0")
                .GET()
                .build();

        HttpResponse<byte[]> resp = httpClient.send(req, BodyHandlers.ofByteArray());
        int sc = resp.statusCode();
        if (sc >= 200 && sc < 300) {
            return resp.body();
        }
        log.warn("Failed to download image {} status={}", imageUrl, sc);
        return null;
    }
}
