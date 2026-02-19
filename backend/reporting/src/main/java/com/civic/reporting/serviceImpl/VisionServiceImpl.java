package com.civic.reporting.serviceImpl;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.civic.reporting.service.VisionResult;
import com.civic.reporting.service.VisionService;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;

@Service
public class VisionServiceImpl implements VisionService {

    @Override
    public Optional<VisionResult> analyzeImage(String imageUrl) {
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

            ByteString imgBytes;
            try (InputStream in = new URL(imageUrl).openStream()) {
                imgBytes = ByteString.readFrom(in);
            }

            Image img = Image.newBuilder().setContent(imgBytes).build();

            Feature feat = Feature.newBuilder()
                    .setType(Feature.Type.LABEL_DETECTION)
                    .build();

            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();

            BatchAnnotateImagesResponse batch = vision.batchAnnotateImages(List.of(request));
            AnnotateImageResponse resp = batch.getResponsesList().get(0);

            String rawJson = JsonFormat.printer().includingDefaultValueFields().print(resp);

            if (resp.getLabelAnnotationsCount() > 0) {
                EntityAnnotation top = resp.getLabelAnnotations(0);
                return Optional.of(new VisionResult(top.getDescription(), top.getScore(), rawJson));
            }

            return Optional.empty();

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @SuppressWarnings("unused")
    private String mapLabelsToCategory(List<EntityAnnotation> labels) {
        for (EntityAnnotation label : labels) {
            String desc = label.getDescription().toLowerCase();
            if (desc.contains("road") || desc.contains("pothole")) {
                return "ROAD";
            }
            if (desc.contains("water") || desc.contains("pipe") || desc.contains("leak")) {
                return "WATER";
            }
            if (desc.contains("garbage") || desc.contains("waste") || desc.contains("trash")) {
                return "GARBAGE";
            }
            if (desc.contains("electric") || desc.contains("light") || desc.contains("streetlight")) {
                return "ELECTRICITY";
            }
        }
        return "GENERAL";
    }
}