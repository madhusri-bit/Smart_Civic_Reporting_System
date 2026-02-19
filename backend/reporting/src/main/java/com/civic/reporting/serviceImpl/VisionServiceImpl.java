package com.civic.reporting.serviceImpl;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.springframework.stereotype.Service;

import com.civic.reporting.service.VisionService;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

@Service
public class VisionServiceImpl implements VisionService {

    @Override
    public String detectCategory(String imageUrl) {
        try {
            ImageAnnotatorClient vision = ImageAnnotatorClient.create();

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

            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(List.of(request));

            List<EntityAnnotation> labels = response.getResponses(0).getLabelAnnotationsList();

            return mapLabelsToCategory(labels);

        } catch (Exception e) {
            e.printStackTrace();
            return "GENERAL";
        }
    }

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