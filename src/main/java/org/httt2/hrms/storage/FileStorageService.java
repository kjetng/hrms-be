package org.httt2.hrms.storage;

import org.httt2.hrms.storage.model.PutPresignedResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {
  String upload(MultipartFile file);
  void delete(String fileName);
  PutPresignedResult getPresignedUrl(String fileName, String contentType);

  List<String> uploadMultiple(List<MultipartFile> files);
  void deleteMultiple(List<String> fileNames);
}
