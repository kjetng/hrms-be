package org.httt2.hrms.activity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.httt2.hrms.employee.entity.Employee;
import java.time.LocalDate;

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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "emp_id", nullable = false)
  @ToString.Exclude
  private Employee employee;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "campaign_id", nullable = false)
  @ToString.Exclude
  private Campaign campaign;
}