package org.httt2.hrms.storage.impl;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.storage.FileStorageService;
import org.httt2.hrms.storage.model.PutPresignedResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "application.storage", name = "type", havingValue = "s3")
public class S3FileStorageService implements FileStorageService {

  private final S3Template s3Template;
  private final RestClient.Builder builder;

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucketName;

  @Override
  public String upload(MultipartFile file) {
    if (file.isEmpty()) throw new RuntimeException("Empty file");
    String uniqueFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
    try (InputStream is = file.getInputStream()) {
      s3Template.upload(bucketName, uniqueFilename, is);
      return uniqueFilename;
    } catch (IOException e) {
      throw new RuntimeException("S3 Upload Failed", e);
    }
  }

  @Override
  public void delete(String fileName) {
    s3Template.deleteObject(bucketName, fileName);
  }

  @Override
  public PutPresignedResult getPresignedUrl(String fileName, String contentType) {
    String uniqueKey = java.util.UUID.randomUUID() + "_" + fileName;
    String url = s3Template.createSignedPutURL(
        bucketName,
        uniqueKey,
        Duration.ofMinutes(30),
        null,
        contentType
    ).toString();

    return PutPresignedResult.builder()
        .key(uniqueKey)
        .url(url)
        .build();
  }

  @Override
  public List<String> uploadMultiple(List<MultipartFile> files) {
    // Reuse the single upload logic for each file
    return files.parallelStream()
        .map(this::upload)
        .toList();
  }

  @Override
  public void deleteMultiple(List<String> fileNames) {
    // delete simultaneously
    fileNames.parallelStream()
        .forEach(this::delete);
  }
}