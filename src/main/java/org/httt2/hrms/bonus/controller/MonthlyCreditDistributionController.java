package org.httt2.hrms.bonus.controller;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.bonus.service.MonthlyCreditDistributionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credits/distribution")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MonthlyCreditDistributionController {

    private final MonthlyCreditDistributionService service;

    /**
     * Manually trigger monthly distribution for demo/testing.
     * If `force=true`, bypass the day-of-month check but still respect
     * idempotence within the current month.
     * Requires ADMIN role.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/monthly")
    public ResponseEntity<String> triggerMonthly(@RequestParam(name = "force", defaultValue = "false") boolean force) {
        service.runMonthlyDistributionNow(force);
        return ResponseEntity.ok("Triggered monthly distribution (force=" + force + ")");
    }
}
