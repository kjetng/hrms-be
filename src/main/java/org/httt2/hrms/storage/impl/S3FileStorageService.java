package org.httt2.hrms.storage.impl;

import io.awspring.cloud.s3.ObjectMetadata; // Import this
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.storage.FileStorageService;
import org.httt2.hrms.storage.model.PutPresignedResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "application.storage", name = "type", havingValue = "s3")
public class S3FileStorageService implements FileStorageService {

  private final S3Template s3Template;

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucketName;

  @Override
  public String upload(MultipartFile file) {
    if (file.isEmpty()) throw new RuntimeException("Empty file");

    String uniqueFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();

    // Add Metadata so S3 knows this is an image/pdf/etc.
    ObjectMetadata metadata = ObjectMetadata.builder()
        .contentType(file.getContentType())
        .contentLength(file.getSize())
        .build();

    try (InputStream is = file.getInputStream()) {
      // Use the overload that accepts metadata
      s3Template.upload(bucketName, uniqueFilename, is, metadata);
      log.info("Uploaded file: {}", uniqueFilename);
      return uniqueFilename;
    } catch (IOException e) {
      log.error("Failed to upload file to S3", e);
      throw new RuntimeException("S3 Upload Failed", e);
    }
  }

  @Override
  public PutPresignedResult getPresignedUrl(String fileName, String contentType) {
    String uniqueKey = UUID.randomUUID() + "_" + fileName;

    URL url = s3Template.createSignedPutURL(
        bucketName,
        uniqueKey,
        Duration.ofMinutes(30),
        null, // metadata
        contentType // Crucial: This restricts the upload to this specific type
    );

    return PutPresignedResult.builder()
        .key(uniqueKey)
        .url(url.toString())
        .build();
  }

  @Override
  public void delete(String fileName) {
    s3Template.deleteObject(bucketName, fileName);
  }

  @Override
  public List<String> uploadMultiple(List<MultipartFile> files) {
    // REFACTOR 3: Avoid parallelStream() for network I/O unless you have a custom pool.
    // A simple stream is safer for stability.
    return files.stream()
        .map(this::upload)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteMultiple(List<String> fileNames) {
    fileNames.forEach(this::delete);
  }
}