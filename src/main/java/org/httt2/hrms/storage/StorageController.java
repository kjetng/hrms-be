package org.httt2.hrms.storage;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.storage.model.PutPresignedResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {

  private final FileStorageService fileStorageService;

//  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//  public ResponseEntity<List<String>> uploadFiles(
//      @RequestPart("files") List<MultipartFile> files
//  ) {
//    List<String> fileKeys = fileStorageService.uploadMultiple(files);
//    return ResponseEntity.ok(fileKeys);
//  }

  /**
   * Deletes one or multiple files by Key.
   */
  @DeleteMapping("/delete")
  public ResponseEntity<Void> deleteFiles(
      @RequestBody List<String> fileKeys
  ) {
    fileStorageService.deleteMultiple(fileKeys);
    return ResponseEntity.noContent().build();
  }

  /**
   * Generates a Presigned URL (valid for 30 minutes) to view a file.
   * Useful when the frontend only has the Key (e.g. "uuid_img.jpg")
   * and needs a valid link to put in <img src="...">
   */
  @GetMapping("/presigned-url")
  public ResponseEntity<PutPresignedResult> getPresignedUrl(
      @RequestParam("fileName") String fileName,
      @RequestParam("contentType") String contentType
  ) {
    return ResponseEntity.ok(fileStorageService.getPresignedUrl(fileName, contentType));
  }
}