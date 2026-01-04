package org.httt2.hrms.activity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime; // Import thêm

@Entity
@Table(name = "employee_activity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeActivity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long activityId;

  private LocalDate activityDate;
  private String metrics; // JSON string or text
  private String proofImage;
  private String status;

  @Column(columnDefinition = "TEXT")
  private String rejectionReason;

  private Long empId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "campaign_id", nullable = false)
  @ToString.Exclude
  private Campaign campaign;

  @Column(updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
      if (createdAt == null) {
          createdAt = LocalDateTime.now();
      }
      if (status == null) {
          status = "pending"; // Mặc định là pending khi mới tạo
      }
  }
}