package org.httt2.hrms.activity.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CampaignCreateRequest {
    private String campaignName;
    private String description;
    private String campaignType; // 'walking', 'running', 'cycling'
    private Double targetGoal;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String imageUrl; // Link ảnh (CloudFront URL) frontend gửi xuống
}