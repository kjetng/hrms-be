package org.httt2.hrms.service;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.entity.Campaign;
import org.httt2.hrms.repository.CampaignRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampaignService {
    
    private final CampaignRepository campaignRepository;
    
    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public Campaign getCampaignById(UUID id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));
    }
    
    public List<Campaign> searchCampaigns(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllCampaigns();
        }
        return campaignRepository.searchCampaigns(searchTerm.trim());
    }
}