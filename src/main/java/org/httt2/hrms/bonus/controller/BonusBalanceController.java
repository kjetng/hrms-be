package org.httt2.hrms.bonus.controller;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.bonus.dto.BonusBalanceResponse;
import org.httt2.hrms.bonus.service.BonusPointViewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for bonus balance endpoints.
 * Provides the /api/bonus/balance endpoint as specified in the dashboard requirements.
 */
@RestController
@RequestMapping("/api/bonus")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BonusBalanceController {

    private final BonusPointViewService viewService;

    /**
     * Get current employee's bonus credit balance.
     * Used for employee dashboard stats.
     * 
     * Response:
     * {
     *   "empId": 123,
     *   "currentBalance": 1500,
     *   "totalRedeemed": 500,
     *   "totalReceived": 2000
     * }
     */
    @GetMapping("/balance")
    public ResponseEntity<BonusBalanceResponse> getBonusBalance() {
        try {
            BonusBalanceResponse balance = viewService.getBonusBalance();
            return ResponseEntity.ok(balance);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
