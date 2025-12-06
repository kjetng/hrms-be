package org.httt2.hrms.activity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.activity.entity.Campaign;
import org.httt2.hrms.activity.repository.CampaignRepository;
import org.springframework.stereotype.Service;
import org.httt2.hrms.activity.dto.CampaignCreateRequest;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;

    public List<Campaign> getAllCampaigns() {
        log.info("Fetching all campaigns");
        return campaignRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Campaign> getCampaignsByStatus(String status) {
        log.info("Fetching campaigns by status: {}", status);
        return campaignRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<Campaign> searchCampaigns(String searchTerm) {
        log.info("Searching campaigns with term: {}", searchTerm);
        return campaignRepository.searchCampaigns(searchTerm);
    }

    public Optional<Campaign> getCampaignById(Long campaignId) {
        log.info("Fetching campaign by ID: {}", campaignId);
        return campaignRepository.findByCampaignId(campaignId);
    }

    public List<Campaign> getActiveCampaigns() {
        log.info("Fetching active campaigns");
        return campaignRepository.findByStatusOrderByCreatedAtDesc("active");
    }

    public Long getTotalCampaigns() {
        return campaignRepository.count();
    }

    public Campaign createCampaign(CampaignCreateRequest request) {
        log.info("Creating campaign: {}", request.getCampaignName());

        // 1. Validate Business Logic
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        // 2. Tự động xác định Primary Metric dựa trên Activity Type
        String metric = determinePrimaryMetric(request.getCampaignType());

        // 3. Map DTO -> Entity
        Campaign campaign = Campaign.builder()
                .campaignName(request.getCampaignName())
                .description(request.getDescription())
                .campaignType(request.getCampaignType())
                .primaryMetric(metric) // Tự động set
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .imageUrl(request.getImageUrl())
                // Status sẽ được @PrePersist trong Entity xử lý thành 'draft', 
                // hoặc bạn có thể set logic 'active' nếu startDate là hôm nay tại đây.
                .status("draft") 
                .build();

        return campaignRepository.save(campaign);
    }

    // Helper method để xác định đơn vị đo lường
    private String determinePrimaryMetric(String type) {
        if (type == null) return "Points";
        switch (type.toLowerCase()) {
            case "walking":
            case "running":
            case "cycling":
                return "Distance (km)";
            default:
                return "Points";
        }
    }
}

