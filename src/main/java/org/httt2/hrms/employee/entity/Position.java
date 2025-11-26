package org.httt2.hrms.employee.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "position")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long positionId;

  @Column(nullable = false)
  private String positionName;

  private String description;

  @Column(nullable = false)
  private BigDecimal salary;
}