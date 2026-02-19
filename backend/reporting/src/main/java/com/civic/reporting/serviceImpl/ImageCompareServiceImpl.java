package com.civic.reporting.serviceImpl;

import org.springframework.stereotype.Service;

import com.civic.reporting.service.ImageCompareService;

@Service
public class ImageCompareServiceImpl implements ImageCompareService {

    @Override
    public double compareImages(String beforeUrl, String afterUrl) {
        return 0.45; // Mock SSIM score
    }
}
