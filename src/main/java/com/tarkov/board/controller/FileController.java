package com.tarkov.board.controller;

import com.tarkov.board.service.MinioService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLConnection;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final MinioService minioService;

    public FileController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@RequestParam("file") MultipartFile file) {
        return minioService.upload(file);
    }

    @GetMapping("/download")
    public void download(@RequestParam("objectName") String objectName, HttpServletResponse response) {
        if (!StringUtils.hasText(objectName) || !minioService.objectExists(objectName)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = URLConnection.guessContentTypeFromName(objectName);
        response.setContentType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "inline; filename=\"" + extractFilename(objectName) + "\"");

        try (InputStream inputStream = minioService.getObject(objectName)) {
            inputStream.transferTo(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            throw new IllegalStateException("Download failed", e);
        }
    }

    private String extractFilename(String objectName) {
        int slashIndex = objectName.lastIndexOf('/');
        if (slashIndex < 0 || slashIndex == objectName.length() - 1) {
            return objectName;
        }
        return objectName.substring(slashIndex + 1);
    }
}
