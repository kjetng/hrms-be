package org.httt2.hrms.bonus.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.httt2.hrms.bonus.dto.BonusCreditSettingRequest;
import org.httt2.hrms.bonus.entity.BonusCreditSetting;
import org.httt2.hrms.bonus.service.BonusCreditSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bonus-settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // adjust for frontend domain
public class BonusCreditSettingController {

    private final BonusCreditSettingService service;

    /**
     * Used when page loads
     */
    @GetMapping
    public ResponseEntity<BonusCreditSetting> getSetting() {
        System.out.println("PING HIT");
        return ResponseEntity.ok(service.getCurrentSetting());
    }

    /**
     * Used when clicking "Save Changes"
     */
    @PostMapping
    public ResponseEntity<BonusCreditSetting> saveSetting(
            @Valid @RequestBody BonusCreditSettingRequest request) {
        System.out.println("HIT CONTROLLER");
        return ResponseEntity.ok(service.saveOrUpdate(request));
    }
}
