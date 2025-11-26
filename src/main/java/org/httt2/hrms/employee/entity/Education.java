package org.httt2.hrms.employee.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "education")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Education {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // Using surrogate key is safer for Education than composite

  @Column(nullable = false)
  private String degree;

  private String fieldOfStudy;
  private Double gpa;
  private String country;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "emp_id", nullable = false)
  @ToString.Exclude
  private Employee employee;
}