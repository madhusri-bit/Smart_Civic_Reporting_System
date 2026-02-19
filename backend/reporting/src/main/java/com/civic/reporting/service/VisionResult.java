package com.civic.reporting.service;

public class VisionResult {
    public final String label;
    public final double confidence;
    public final String rawJson;

    public VisionResult(String label, double confidence, String rawJson) {
        this.label = label;
        this.confidence = confidence;
        this.rawJson = rawJson;
    }
}
