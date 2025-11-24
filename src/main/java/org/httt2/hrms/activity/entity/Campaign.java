package org.httt2.hrms.activity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "campaign")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long campaignId;

  private String campaignName;
  private String campaignType;
  private String primaryMetric;
  private String description;
  private LocalDate startDate;
  private LocalDate endDate;
}