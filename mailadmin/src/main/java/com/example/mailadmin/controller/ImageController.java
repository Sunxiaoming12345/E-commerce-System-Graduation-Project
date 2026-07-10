package com.example.mailadmin.controller;

import com.example.result.Result;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ImageController {

    private final MinioClient minioClient;

    @Value("${minio.url}")
    private String minioEndpoint;

    @Value("${minio.bucket.name}")
    private String bucketName;

    @PostMapping("/user/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) throw new RuntimeException("文件为空");
            String ext = file.getOriginalFilename() != null
                    && file.getOriginalFilename().contains(".")
                    ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."))
                    : ".png";
            String objectName = "avatars/" + UUID.randomUUID() + ext;
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            String url = minioEndpoint + "/" + bucketName + "/" + objectName;
            return Result.success(Map.of("url", url));
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
