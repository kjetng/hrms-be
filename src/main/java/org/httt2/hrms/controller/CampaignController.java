package org.httt2.hrms.controller;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.dto.ApiResponse;
import org.httt2.hrms.dto.CampaignDTO;
import org.httt2.hrms.service.CampaignService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/HRMS-FE") // Vite dev server
public class CampaignController {
    
    private final CampaignService campaignService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<CampaignDTO>>> getAllCampaigns(
            @RequestParam(required = false) String search) {
        
        try {
            List<CampaignDTO> campaigns;
            
            if (search != null && !search.trim().isEmpty()) {
                campaigns = campaignService.searchCampaigns(search)
                        .stream()
                        .map(CampaignDTO::fromEntity)
                        .collect(Collectors.toList());
            } else {
                campaigns = campaignService.getAllCampaigns()
                        .stream()
                        .map(CampaignDTO::fromEntity)
                        .collect(Collectors.toList());
            }
            
            return ResponseEntity.ok(ApiResponse.success(campaigns, "Campaigns retrieved successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.success(List.of(), "Error retrieving campaigns: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignDTO>> getCampaignById(@PathVariable UUID id) {
        try {
            var campaign = campaignService.getCampaignById(id);
            return ResponseEntity.ok(ApiResponse.success(CampaignDTO.fromEntity(campaign), "Campaign retrieved successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}