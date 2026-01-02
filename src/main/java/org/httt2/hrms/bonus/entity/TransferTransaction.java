package org.httt2.hrms.bonus.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferTransaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long transferId;

  private Integer numberPoint;
  private String note;

  @Enumerated(EnumType.STRING)
  @Column(name = "transfer_type")
  private TransferType type;

  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id")
  @ToString.Exclude
  private BonusPointAccount sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id")
  @ToString.Exclude
  private BonusPointAccount receiver;
}