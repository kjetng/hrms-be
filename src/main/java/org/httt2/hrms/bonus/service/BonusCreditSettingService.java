package org.httt2.hrms.bonus.service;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.bonus.dto.BonusCreditSettingRequest;
import org.httt2.hrms.bonus.entity.BonusCreditSetting;
import org.httt2.hrms.bonus.repository.BonusCreditSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BonusCreditSettingService {

    private final BonusCreditSettingRepository repository;

    /**
     * Fetch current bonus credit setting
     * Used when page loads
     */
    public BonusCreditSetting getCurrentSetting() {
        return repository.findAll()
                .stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Create or update bonus credit setting
     * Used when clicking "Save Changes"
     */
    public BonusCreditSetting saveOrUpdate(BonusCreditSettingRequest request) {

        BonusCreditSetting setting = getCurrentSetting();

        if (setting == null) {
            setting = BonusCreditSetting.builder().build();
        }

        setting.setBaseBonusCredits(request.getBaseBonusCredits());
        setting.setConversionRate(request.getConversionRate());
        setting.setCreditDate(request.getDate());

        return repository.save(setting);
    }
}
