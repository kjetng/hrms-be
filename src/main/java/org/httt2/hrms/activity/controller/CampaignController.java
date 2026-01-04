package org.httt2.hrms.activity.controller;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.activity.entity.Campaign;
import org.httt2.hrms.activity.entity.EmployeeActivity;
import org.httt2.hrms.activity.service.CampaignService;
import org.httt2.hrms.auth.config.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.httt2.hrms.activity.dto.ActivitySubmissionRequest;
import org.httt2.hrms.activity.dto.CampaignCreateRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;
import java.security.Principal;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow frontend connection
public class CampaignController {

    private final CampaignService campaignService;
    private final JwtService jwtService;

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
    
    // EMPLOYEE:Lấy danh sách campaign đang mở (Cho phần "You Can Join")
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

    @PostMapping("/{id}/publish")
    public ResponseEntity<Campaign> publishCampaign(@PathVariable Long id) {
        // Gọi service để đổi status -> "active"
        Campaign publishedCampaign = campaignService.publishCampaign(id);
        return ResponseEntity.ok(publishedCampaign);
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<?> registerCampaign(
            @PathVariable Long id,
            Principal principal,
            HttpServletRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized. Please login first.");
        }

        try {
            String userEmail = principal.getName();
            Long empId = jwtService.extractEmpIdFromRequest(request);
            
            campaignService.registerForCampaign(id, userEmail, empId);
            return ResponseEntity.ok("Successfully registered for the campaign!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
        }
    }

    // EMPLOYEE: Lấy danh sách campaign của tôi (Cho phần "My Active Campaigns")
    @GetMapping("/my-campaigns")
    public ResponseEntity<List<Campaign>> getMyCampaigns(Principal principal, HttpServletRequest request) {
        if (principal == null) return ResponseEntity.status(401).build();

        Long empId = jwtService.extractEmpIdFromRequest(request);
        
        return ResponseEntity.ok(campaignService.getMyCampaigns(principal.getName(), empId));
    }

    
    // SUBMIT ACTIVITY
    @PostMapping("/{id}/activities")
    public ResponseEntity<?> submitActivity(
            @PathVariable Long id,
            @RequestBody ActivitySubmissionRequest request,
            HttpServletRequest httpServletRequest
    ) {
        try {
            Long empId = jwtService.extractEmpIdFromRequest(httpServletRequest);
            if (empId == null) return ResponseEntity.status(401).body("Employee ID not found in token");

            EmployeeActivity activity = campaignService.submitActivity(id, empId, request);
            return ResponseEntity.ok(activity);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }


    // VIEW SUBMISSIONS (HISTORY)
    @GetMapping("/{id}/activities/me")
    public ResponseEntity<?> getMyActivities(
            @PathVariable Long id,
            HttpServletRequest httpServletRequest
    ) {
        try {
            Long empId = jwtService.extractEmpIdFromRequest(httpServletRequest);
            if (empId == null) return ResponseEntity.status(401).body("Employee ID not found in token");

            List<EmployeeActivity> activities = campaignService.getMyCampaignActivities(id, empId);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }


    // DELETE ACTIVITY
    @DeleteMapping("/activities/{activityId}")
    public ResponseEntity<?> deleteActivity(
            @PathVariable Long activityId,
            HttpServletRequest request
    ) {
        try {
            Long empId = jwtService.extractEmpIdFromRequest(request);
            if (empId == null) return ResponseEntity.status(401).body("Unauthorized");

            campaignService.deleteActivity(activityId, empId);
            return ResponseEntity.ok("Activity deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }


    // UPDATE ACTIVITY
    @PutMapping("/activities/{activityId}")
    public ResponseEntity<?> updateActivity(
            @PathVariable Long activityId,
            @RequestBody ActivitySubmissionRequest request,
            HttpServletRequest httpServletRequest
    ) {
        try {
            Long empId = jwtService.extractEmpIdFromRequest(httpServletRequest);
            if (empId == null) return ResponseEntity.status(401).body("Unauthorized");

            EmployeeActivity updatedActivity = campaignService.updateActivity(activityId, empId, request);
            return ResponseEntity.ok(updatedActivity);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}


