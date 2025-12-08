package org.httt2.hrms.activity.controller;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.activity.entity.Campaign;
import org.httt2.hrms.activity.service.CampaignService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.httt2.hrms.activity.dto.CampaignCreateRequest;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow frontend connection
public class CampaignController {

    private final CampaignService campaignService;

    @GetMapping
    public ResponseEntity<List<Campaign>> getAllCampaigns() {
        try {
            List<Campaign> campaigns = campaignService.getAllCampaigns();
            return ResponseEntity.ok(campaigns);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{campaignId}")
    public ResponseEntity<Campaign> getCampaignById(@PathVariable Long campaignId) {
        try {
            Optional<Campaign> campaign = campaignService.getCampaignById(campaignId);
            return campaign.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Campaign>> getCampaignsByStatus(@PathVariable String status) {
        try {
            List<Campaign> campaigns = campaignService.getCampaignsByStatus(status);
            return ResponseEntity.ok(campaigns);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Campaign>> searchCampaigns(@RequestParam String q) {
        try {
            if (q == null || q.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            List<Campaign> campaigns = campaignService.searchCampaigns(q.trim());
            return ResponseEntity.ok(campaigns);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<Campaign>> getActiveCampaigns() {
        try {
            List<Campaign> campaigns = campaignService.getActiveCampaigns();
            return ResponseEntity.ok(campaigns);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTotalCampaigns() {
        try {
            Long count = campaignService.getTotalCampaigns();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createCampaign(@RequestBody CampaignCreateRequest request) {
        try {
            // Gọi Service xử lý
            Campaign newCampaign = campaignService.createCampaign(request);
            return ResponseEntity.ok(newCampaign);
        } catch (IllegalArgumentException e) {
            // Trả về lỗi 400 nếu dữ liệu không hợp lệ (vd: ngày kết thúc < bắt đầu)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi để debug
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCampaign(@PathVariable Long id, @RequestBody CampaignCreateRequest request) {
        try {
            Campaign updatedCampaign = campaignService.updateCampaign(id, request);
            return ResponseEntity.ok(updatedCampaign);
        } catch (RuntimeException e) {
            // Bắt lỗi không tìm thấy ID hoặc lỗi validate ngày tháng
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}