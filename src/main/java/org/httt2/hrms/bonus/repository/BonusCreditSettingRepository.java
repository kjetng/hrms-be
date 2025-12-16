package org.httt2.hrms.bonus.repository;

import org.httt2.hrms.bonus.entity.BonusCreditSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BonusCreditSettingRepository
        extends JpaRepository<BonusCreditSetting, Long> {
}
