package org.httt2.hrms.activity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.httt2.hrms.activity.entity.id.CampaignParticipantId;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaign_participant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(CampaignParticipantId.class)
public class CampaignParticipant {

  @Id
  private Long empId;

  @Id
  private Long campaignId;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("campaignId") // Maps to the key class
  @JoinColumn(name = "campaign_id")
  private Campaign campaign;

  private Double currentScore;

  private LocalDateTime joinedAt;

  @PrePersist
  protected void onCreate() {
    if (currentScore == null) currentScore = 0.0;
  }
}

