package org.httt2.hrms.bonus.entity;

import jakarta.persistence.*;
import lombok.*;

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
}