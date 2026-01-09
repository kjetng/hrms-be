package org.httt2.hrms.activity.dto.approval;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PendingCampaignDTO {
    private Long id;
    private String name;
    private Long pendingCount;
}