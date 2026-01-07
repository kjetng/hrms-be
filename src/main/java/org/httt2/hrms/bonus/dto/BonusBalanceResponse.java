package org.httt2.hrms.bonus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for employee bonus balance used in dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonusBalanceResponse {
    private Long empId;
    private Integer currentBalance;
    private Integer totalRedeemed;
    private Integer totalReceived;
}
