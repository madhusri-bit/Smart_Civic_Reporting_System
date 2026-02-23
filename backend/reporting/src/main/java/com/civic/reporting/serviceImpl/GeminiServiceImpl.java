// package com.civic.reporting.serviceImpl;

// import java.io.IOException;
// import java.net.URI;
// import java.net.http.HttpClient;
// import java.net.http.HttpRequest;
// import java.net.http.HttpRequest.BodyPublishers;
// import java.net.http.HttpResponse;
// import java.net.http.HttpResponse.BodyHandlers;
// import java.time.Duration;
// import java.util.Base64;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;

// import com.civic.reporting.service.GeminiResult;
// import com.civic.reporting.service.GeminiService;
// import com.civic.reporting.service.VisionResult;
// import com.civic.reporting.service.VisionService;

// import lombok.RequiredArgsConstructor;
// import tools.jackson.databind.ObjectMapper;

// @Service
// @RequiredArgsConstructor
// public class GeminiServiceImpl implements GeminiService {

//     private static final Logger log = LoggerFactory.getLogger(GeminiServiceImpl.class);
//     private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

//     private final VisionService visionService;

//     @Value("${gemini.api.url:https://api.example.com/v1/gemini}")
//     private String geminiApiUrl;

//     @Value("${gemini.api.key:}")
//     private String geminiApiKey;

//     @Value("${gemini.model:gemini-1.5-flash}")
//     private String geminiModel;

//     private final HttpClient httpClient = HttpClient.newBuilder()
//             .connectTimeout(Duration.ofSeconds(10))
//             .followRedirects(HttpClient.Redirect.NORMAL)
//             .build();

//     @Override
//     public Optional<GeminiResult> analyzeImage(String imageUrl) {
//         try {
//             byte[] imageBytes = downloadImage(imageUrl);
//             if (imageBytes == null || imageBytes.length == 0) {
//                 log.warn("Downloaded image is empty: {}", imageUrl);
//                 return Optional.empty();
//             }

//             String base64 = Base64.getEncoder().encodeToString(imageBytes);

//             Map<String, Object> payload = Map.of(
//                     "contents", List.of(
//                             Map.of(
//                                     "parts", List.of(
//                                             Map.of(
//                                                     "text",
//                                                     "Analyze this civic issue image and return ONLY JSON with fields: "
//                                                             +
//                                                             "title, description, category (ROAD/WATER/GARBAGE/ELECTRICITY/GENERAL), department."),
//                                             Map.of(
//                                                     "inline_data", Map.of(
//                                                             "mime_type", "image/jpeg",
//                                                             "data", base64))))));

//             String body = OBJECT_MAPPER.writeValueAsString(payload);
//             String fullUrl = geminiApiUrl + "?key=" + geminiApiKey;
//             HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
//                     .uri(URI.create(fullUrl))
//                     .timeout(Duration.ofSeconds(30))
//                     .header("Content-Type", "application/json")
//                     .header("User-Agent", "civic-reporting/1.0")
//                     .POST(BodyPublishers.ofString(body));

//             HttpRequest request = reqBuilder.build();
//             HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

//             int status = response.statusCode();
//             String respBody = response.body();
//             if (status < 200 || status >= 300) {
//                 log.warn("Gemini API returned non-2xx status: {} body={}", status, respBody);
//                 return Optional.empty();
//             }

//             // Try to parse the response directly into GeminiResult. Some Gemini
//             // integrations
//             // return plain JSON, others return text containing JSON. We attempt the common
//             // cases to be resilient.
//             try {
//                 GeminiResult direct = OBJECT_MAPPER.readValue(respBody, GeminiResult.class);
//                 return Optional.ofNullable(direct);
//             } catch (Exception e) {
//                 // attempt to extract first JSON object from response text
//                 int start = respBody.indexOf('{');
//                 int end = respBody.lastIndexOf('}');
//                 if (start >= 0 && end > start) {
//                     String json = respBody.substring(start, end + 1);
//                     try {
//                         GeminiResult parsed = OBJECT_MAPPER.readValue(json, GeminiResult.class);
//                         return Optional.ofNullable(parsed);
//                     } catch (Exception ex) {
//                         log.debug("Failed parsing extracted JSON from Gemini response", ex);
//                     }
//                 }
//                 log.warn("Unable to parse Gemini response into GeminiResult: {}", respBody);
//             }

//         } catch (IOException | InterruptedException ex) {
//             log.error("Error calling Gemini API for image {}", imageUrl, ex);
//             Thread.currentThread().interrupt();
//         } catch (Exception ex) {
//             log.error("Unexpected error analyzing image {}", imageUrl, ex);
//         }

//         // Fallback: attempt to use VisionService if available to produce basic fields.
//         try {
//             Optional<VisionResult> vr = visionService.analyzeImage(imageUrl);
//             if (vr.isPresent()) {
//                 VisionResult v = vr.get();
//                 GeminiResult fallback = new GeminiResult();
//                 String label = v.label != null ? v.label : "GENERAL";
//                 fallback.setTitle("Report: " + label);
//                 fallback.setDescription("Auto-generated description: " + label);
//                 fallback.setCategory(label);
//                 fallback.setDepartment("General");
//                 return Optional.of(fallback);
//             }
//         } catch (Exception e) {
//             log.debug("VisionService fallback failed", e);
//         }

//         return Optional.empty();
//     }

//     private byte[] downloadImage(String imageUrl) throws IOException, InterruptedException {
//         HttpRequest req = HttpRequest.newBuilder()
//                 .uri(URI.create(imageUrl))
//                 .timeout(Duration.ofSeconds(20))
//                 .header("User-Agent", "civic-reporting-image-fetcher/1.0")
//                 .GET()
//                 .build();

//         HttpResponse<byte[]> resp = httpClient.send(req, BodyHandlers.ofByteArray());
//         int sc = resp.statusCode();
//         if (sc >= 200 && sc < 300) {
//             return resp.body();
//         }
//         log.warn("Failed to download image {} status={}", imageUrl, sc);
//         return null;
//     }
// }
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
import java.util.List;
import java.util.Map;
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
    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    public Optional<GeminiResult> analyzeImage(String imageUrl) {

        try {
            log.info("Calling Gemini for image: {}", imageUrl);

            byte[] imageBytes = downloadImage(imageUrl);
            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("Downloaded image is empty");
                return Optional.empty();
            }

            String base64 = Base64.getEncoder().encodeToString(imageBytes);

            Map<String, Object> payload = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of(
                                                    "text",
                                                    """
                                                            You are a strict JSON API.
                                                            Analyze this civic issue image.
                                                            Respond ONLY with valid JSON:
                                                            {
                                                              "title": "...",
                                                              "description": "...",
                                                              "category": "ROAD | WATER | GARBAGE | ELECTRICITY | GENERAL",
                                                              "department": "..."
                                                            }
                                                            """),
                                            Map.of(
                                                    "inline_data", Map.of(
                                                            "mime_type", "image/jpeg",
                                                            "data", base64))))));

            String body = OBJECT_MAPPER.writeValueAsString(payload);

            if (geminiApiKey == null || geminiApiKey.isBlank()) {
                log.error("Gemini API key is empty. Check gemini.api.key configuration.");
                return Optional.empty();
            }
            String endpoint = resolveGeminiEndpoint();
            log.info("Gemini endpoint: {}", endpoint);
            log.info("Gemini key fingerprint: {}", maskApiKey(geminiApiKey));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", geminiApiKey)
                    .POST(BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            log.info("Gemini Status Code: {}", response.statusCode());
            log.info("RAW GEMINI RESPONSE: {}", response.body());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("Gemini API Error → {}", response.body());
                return Optional.empty();
            }

            return parseGeminiResponse(response.body());

        } catch (Exception e) {
            log.error("Gemini call failed", e);
        }

        // ✅ Vision fallback
        try {
            Optional<VisionResult> vr = visionService.analyzeImage(imageUrl);
            if (vr.isPresent()) {
                VisionResult v = vr.get();

                GeminiResult fallback = new GeminiResult();
                String label = v.label != null ? v.label : "GENERAL";

                fallback.setTitle("Report: " + label);
                fallback.setDescription("Detected via Vision: " + label);
                fallback.setCategory(label);
                fallback.setDepartment("General");

                log.info("Using Vision fallback → {}", label);

                return Optional.of(fallback);
            }
        } catch (Exception ex) {
            log.warn("Vision fallback failed", ex);
        }

        return Optional.empty();
    }

    private Optional<GeminiResult> parseGeminiResponse(String respBody) {

        try {
            Map<String, Object> json = OBJECT_MAPPER.readValue(respBody, Map.class);

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) json.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                log.warn("No candidates returned by Gemini");
                return Optional.empty();
            }

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");

            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                log.warn("No parts in Gemini response");
                return Optional.empty();
            }

            String text = parts.get(0).get("text").toString();
            log.info("Gemini TEXT PART: {}", text);

            int start = text.indexOf('{');
            int end = text.lastIndexOf('}');

            if (start < 0 || end <= start) {
                log.warn("Gemini did not return JSON, using fallback text");

                GeminiResult fallback = new GeminiResult();
                fallback.setTitle("Reported Issue");
                fallback.setDescription(text);
                fallback.setCategory("GENERAL");
                fallback.setDepartment("General");

                return Optional.of(fallback);
            }

            String cleanJson = text.substring(start, end + 1);
            log.info("Extracted JSON: {}", cleanJson);

            GeminiResult result = OBJECT_MAPPER.readValue(cleanJson, GeminiResult.class);

            return Optional.of(result);

        } catch (Exception e) {
            log.error("Failed parsing Gemini response", e);
        }

        return Optional.empty();
    }

    private byte[] downloadImage(String imageUrl) throws IOException, InterruptedException {

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        HttpResponse<byte[]> resp = httpClient.send(req, BodyHandlers.ofByteArray());

        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            log.info("Image downloaded successfully");
            return resp.body();
        }

        log.error("Failed to download image → status {}", resp.statusCode());
        return null;
    }

    private String resolveGeminiEndpoint() {
        String endpoint = (geminiApiUrl == null || geminiApiUrl.isBlank())
                ? "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent"
                : geminiApiUrl.trim();
        int keyParamIndex = endpoint.indexOf("?key=");
        if (keyParamIndex >= 0) {
            endpoint = endpoint.substring(0, keyParamIndex);
        }
        return endpoint;
    }

    private String maskApiKey(String key) {
        if (key == null || key.isBlank()) {
            return "(empty)";
        }
        if (key.length() <= 10) {
            return key.substring(0, Math.min(3, key.length())) + "...";
        }
        return key.substring(0, 6) + "..." + key.substring(key.length() - 4);
    }
}
