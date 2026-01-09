package org.httt2.hrms.activity.controller;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.activity.dto.approval.PendingActivityDTO;
import org.httt2.hrms.activity.dto.approval.PendingCampaignDTO;
import org.httt2.hrms.activity.dto.approval.RejectRequest;
import org.httt2.hrms.activity.service.AdminApprovalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/approvals")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminApprovalController {

    private final AdminApprovalService approvalService;

    // 1. Dashboard: Lấy danh sách các campaign cần duyệt
    @GetMapping("/campaigns")
    public ResponseEntity<List<PendingCampaignDTO>> getPendingCampaigns() {
        return ResponseEntity.ok(approvalService.getCampaignsWithPending());
    }

    // 2. Review Page: Lấy chi tiết các bài pending của 1 campaign
    @GetMapping("/campaigns/{campaignId}/activities")
    public ResponseEntity<List<PendingActivityDTO>> getPendingActivities(@PathVariable Long campaignId) {
        return ResponseEntity.ok(approvalService.getPendingActivities(campaignId));
    }

    // 3. Approve
    @PostMapping("/{activityId}/approve")
    public ResponseEntity<String> approveActivity(@PathVariable Long activityId) {
        try {
            approvalService.approveActivity(activityId);
            return ResponseEntity.ok("Activity approved and score updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Reject
    @PostMapping("/{activityId}/reject")
    public ResponseEntity<String> rejectActivity(@PathVariable Long activityId, @RequestBody RejectRequest request) {
        try {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Rejection reason is required");
            }
            approvalService.rejectActivity(activityId, request.getReason());
            return ResponseEntity.ok("Activity rejected successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}