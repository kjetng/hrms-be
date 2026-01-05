package org.httt2.hrms.bonus.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.httt2.hrms.auth.entity.User;
import org.httt2.hrms.bonus.dto.BonusCreditSettingDto;
import org.httt2.hrms.bonus.entity.BonusCreditSetting;
import org.httt2.hrms.bonus.service.BonusCreditSettingService;
import org.httt2.hrms.bonus.mapper.BonusCreditSettingMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credits/settings")
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
        BonusCreditSettingDto dto = mapper.toDto(entity);
        dto.setRole(extractRoleFromRequest());
        return ResponseEntity.ok(dto);
    }

    /**
     * Used when clicking "Save Changes"
     */
    @PostMapping
    public ResponseEntity<BonusCreditSettingDto> saveSetting(
            @Valid @RequestBody BonusCreditSettingDto request) {
        BonusCreditSetting saved = service.saveOrUpdate(request);
        BonusCreditSettingDto dto = mapper.toDto(saved);
        dto.setRole(extractRoleFromRequest());
        return ResponseEntity.ok(dto);
    }

    private String extractRoleFromRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails && userDetails instanceof User user) {
                return user.getRole() != null ? user.getRole().name() : null;
            }
        }
        return null;
    }

}
