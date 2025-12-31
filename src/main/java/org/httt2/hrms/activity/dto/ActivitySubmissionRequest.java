package org.httt2.hrms.activity.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ActivitySubmissionRequest {
    private LocalDate activityDate;
    private String metrics;    // JSON String: {"distance": 5.2}
    private String proofImage; // URL ảnh từ S3
}