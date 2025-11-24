package org.httt2.hrms.activity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.httt2.hrms.activity.entity.id.CampaignParticipantId;
import org.httt2.hrms.employee.entity.Employee;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaign_participant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignParticipant {

  @EmbeddedId
  private CampaignParticipantId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("empId") // Maps to the key class
  @JoinColumn(name = "emp_id")
  private Employee employee;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("campaignId") // Maps to the key class
  @JoinColumn(name = "campaign_id")
  private Campaign campaign;

  private LocalDateTime joinedAt;
}

