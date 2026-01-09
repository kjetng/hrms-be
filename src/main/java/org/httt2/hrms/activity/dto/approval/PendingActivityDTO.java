package org.httt2.hrms.activity.dto.approval;

import lombok.AllArgsConstructor; // <--- THÊM
import lombok.Builder;           // <--- THÊM
import lombok.Data;
import lombok.NoArgsConstructor; // <--- THÊM

import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Builder             // <--- QUAN TRỌNG: Để dùng được .builder()
@AllArgsConstructor  // <--- QUAN TRỌNG: @Builder cần constructor này
@NoArgsConstructor   // <--- Cần thiết cho Jackson (JSON parsing)
public class PendingActivityDTO {
    private Long id; 
    private String employeeName; 
    private String employeeEmail;
    private LocalDateTime submittedDate;
    private LocalDate activityDate;
    private String metrics;
    private String proofImage;
    private String status;
}