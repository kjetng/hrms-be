package org.httt2.hrms.employee.entity;

import jakarta.persistence.*;
import lombok.*;
import org.httt2.hrms.bonus.entity.BonusPointAccount;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long empId;

  @Column(nullable = false)
  private String fullName;

  @Column(nullable = false, unique = true)
  private String email;

  private String phoneNumber;
  private String permanentAddress;
  private String currentAddress;
  private LocalDate startDate;
  private String jobLevel;
  private String employeeType;
  private String timeType;

  // --- Relationships ---

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dept_id")
  @ToString.Exclude
  private Department department;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "position_id")
  @ToString.Exclude
  private Position position;

  // Self-Referencing Manager
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "manager_id")
  @ToString.Exclude
  private Employee manager;

  // 1-to-1 Mapping to Bonus Account
  @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @ToString.Exclude
  private BonusPointAccount bonusAccount;

  // Collections (Education/Bank) mapped by their owner side
  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
  @ToString.Exclude
  private List<BankAccount> bankAccounts;

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
  @ToString.Exclude
  private List<Education> educations;
}