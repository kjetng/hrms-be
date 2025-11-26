package org.httt2.hrms.employee.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "department")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long deptId;

  @Column(nullable = false)
  private String deptName;

  private String location;

  // Circular Dependency: Department leads (Manager)
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "manager_id")
  @ToString.Exclude
  private Employee manager;
}