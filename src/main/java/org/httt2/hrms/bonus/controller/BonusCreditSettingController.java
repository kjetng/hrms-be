package org.httt2.hrms.bonus.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.httt2.hrms.bonus.dto.BonusCreditSettingDto;
import org.httt2.hrms.bonus.entity.BonusCreditSetting;
import org.httt2.hrms.bonus.service.BonusCreditSettingService;
import org.httt2.hrms.bonus.mapper.BonusCreditSettingMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bonus-settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // adjust for frontend domain
public class BonusCreditSettingController {

    private final BonusCreditSettingService service;
    private final BonusCreditSettingMapper mapper;
    /**
     * Used when page loads
     */
    @GetMapping
    public ResponseEntity<BonusCreditSettingDto> getSetting() {
        BonusCreditSetting entity = service.getCurrentSetting();
        return ResponseEntity.ok(mapper.toDto(entity));
    }

    /**
     * Used when clicking "Save Changes"
     */
    @PostMapping
    public ResponseEntity<BonusCreditSettingDto> saveSetting(
            @Valid @RequestBody BonusCreditSettingDto request) {
        BonusCreditSetting saved = service.saveOrUpdate(request);
        return ResponseEntity.ok(mapper.toDto(saved));
    }
}
