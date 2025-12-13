package org.httt2.hrms.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StorageController {

    private final S3Presigner s3Presigner; // Bean này cần được cấu hình trong AppConfig

    // Lấy thông tin từ file cấu hình hoặc biến môi trường
    @Value("${aws.s3.bucket:htt2-hrms-bucket}") 
    private String bucketName;

    @GetMapping("/presigned-url")
    public ResponseEntity<String> getPresignedUrl(
            @RequestParam String extension,
            @RequestParam String contentType // 1. Thêm tham số này để nhận đúng loại file từ Frontend
    ) {
        // Tạo tên file ngẫu nhiên để tránh trùng lặp: campaigns/uuid.jpg
        String key = "campaigns/" + UUID.randomUUID().toString() + "." + extension;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType) // vd: image/jpeg
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // Link sống trong 10 phút
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        
        // Trả về URL để upload
        return ResponseEntity.ok(presignedRequest.url().toString());
    }
}