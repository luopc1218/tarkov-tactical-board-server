package com.tarkov.board.controller;

import com.tarkov.board.service.MinioService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

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
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + objectName + "\"");

        try (InputStream inputStream = minioService.getObject(objectName)) {
            inputStream.transferTo(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            throw new IllegalStateException("Download failed", e);
        }
    }
}
