package com.zivro.controller;

import com.zivro.dto.ImageAnalysisResponse;
import com.zivro.service.ImageAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAnalysisController {

    private final ImageAnalysisService imageAnalysisService;

    @PostMapping(value = "/analyze-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageAnalysisResponse analyze(
            @RequestPart("image") MultipartFile image,
            @RequestParam(value = "serviceIconKey", required = false) String serviceIconKey) {
        return imageAnalysisService.analyze(image, serviceIconKey);
    }
}
