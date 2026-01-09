package org.httt2.hrms.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for campaigns with participation statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignWithStatsResponse {
    private Long campaignId;
    private String campaignName;
    private String campaignType;
    private String primaryMetric;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String imageUrl;
    private LocalDateTime createdAt;
    
    // Enhanced stats fields
    private Long participants;
    private Double totalDistance;
    private Long pendingSubmissions;
}
