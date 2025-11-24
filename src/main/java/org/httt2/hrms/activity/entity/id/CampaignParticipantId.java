package org.httt2.hrms.activity.entity.id;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Key Class
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignParticipantId implements java.io.Serializable {
  private Long empId;
  private Long campaignId;
}
