package org.httt2.hrms.request.entity;

import jakarta.persistence.*;
import lombok.*;
import org.httt2.hrms.employee.entity.Employee;
import java.time.LocalDate;

@Entity
@Table(name = "request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long requestId;

  @Column(nullable = false)
  private String requestType;

  private LocalDate startDate;
  private LocalDate endDate;
  private String reason;
  private String status;

  // Submits
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "requester_id", nullable = false)
  @ToString.Exclude
  private Employee requester;

  // Approves (Manager)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "approver_id")
  @ToString.Exclude
  private Employee approver;
}