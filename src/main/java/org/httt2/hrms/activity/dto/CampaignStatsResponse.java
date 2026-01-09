package org.httt2.hrms.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for campaign statistics used in admin dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignStatsResponse {
    private Long totalCampaigns;
    private Long activeCampaigns;
    private Long completedCampaigns;
    private Long draftCampaigns;
}
