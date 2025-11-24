package org.httt2.hrms.bonus.entity;

import jakarta.persistence.*;
import lombok.*;
import org.httt2.hrms.employee.entity.Employee;

@Entity
@Table(name = "bonus_point_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonusPointAccount {

  @Id
  private Long empId; // Same ID as Employee

  private Integer bonusPoint;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "emp_id")
  @ToString.Exclude
  private Employee employee;
}