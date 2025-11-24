package org.httt2.hrms.bonus.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "redemption_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedemptionTransaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long redemptionId;

  private Integer convertedPoint;
  private BigDecimal amountReceived;

  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "emp_id")
  @ToString.Exclude
  private BonusPointAccount account;
}