package org.httt2.hrms.bonus.mapper;

import org.httt2.hrms.bonus.dto.BonusCreditSettingDto;
import org.httt2.hrms.bonus.entity.BonusCreditSetting;
import org.springframework.stereotype.Component;

@Component
public class BonusCreditSettingMapper {

    public BonusCreditSettingDto toDto(BonusCreditSetting entity) {
        if (entity == null) return null;

        BonusCreditSettingDto dto = new BonusCreditSettingDto();
        dto.setDate(entity.getCreditDate());
        dto.setBaseBonusCredits(entity.getBaseBonusCredits());
        dto.setConversionRate(entity.getConversionRate());
        return dto;
    }


}
