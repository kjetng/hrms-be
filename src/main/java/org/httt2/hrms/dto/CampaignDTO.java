package org.httt2.hrms.dto;

import lombok.Data;
import org.httt2.hrms.entity.Campaign;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CampaignDTO {
    private UUID id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalTime startTime;
    private LocalDate endDate;
    private LocalTime endTime;
    private Campaign.ActivityType activityType;
    private Campaign.CampaignStatus status;
    private String imageUrl;
    private UUID createdBy;
    
    public static CampaignDTO fromEntity(Campaign campaign) {
        CampaignDTO dto = new CampaignDTO();
        dto.setId(campaign.getId());
        dto.setName(campaign.getName());
        dto.setDescription(campaign.getDescription());
        dto.setStartDate(campaign.getStartDate());
        dto.setStartTime(campaign.getStartTime());
        dto.setEndDate(campaign.getEndDate());
        dto.setEndTime(campaign.getEndTime());
        dto.setActivityType(campaign.getActivityType());
        dto.setStatus(campaign.getStatus());
        dto.setImageUrl(campaign.getImageUrl());
        dto.setCreatedBy(campaign.getCreatedBy());
        return dto;
    }
}