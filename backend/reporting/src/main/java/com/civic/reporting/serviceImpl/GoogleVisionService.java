package com.civic.reporting.serviceImpl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;

import com.civic.reporting.service.VisionService;
import com.civic.reporting.service.VisionResult;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageSource;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.util.JsonFormat;

import java.util.Arrays;

@Service
@Primary
public class GoogleVisionService implements VisionService {

    @Override
    public Optional<VisionResult> analyzeImage(String imageUri) {
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
            ImageSource src = ImageSource.newBuilder().setImageUri(imageUri).build();
            Image img = Image.newBuilder().setSource(src).build();

            Feature labelFeat = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
            Feature webFeat = Feature.newBuilder().setType(Type.WEB_DETECTION).build();

            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addAllFeatures(Arrays.asList(labelFeat, webFeat))
                    .setImage(img)
                    .build();

            BatchAnnotateImagesResponse batchResp = vision.batchAnnotateImages(Arrays.asList(request));
            AnnotateImageResponse resp = batchResp.getResponsesList().get(0);

            String rawJson = JsonFormat.printer().includingDefaultValueFields().print(resp);

            if (resp.getLabelAnnotationsCount() > 0) {
                String label = resp.getLabelAnnotations(0).getDescription();
                double confidence = resp.getLabelAnnotations(0).getScore();
                return Optional.of(new VisionResult(label, confidence, rawJson));
            }

            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
