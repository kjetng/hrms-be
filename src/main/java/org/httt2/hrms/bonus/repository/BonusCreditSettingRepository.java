package org.httt2.hrms.bonus.repository;

import org.httt2.hrms.bonus.entity.BonusCreditSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BonusCreditSettingRepository
        extends JpaRepository<BonusCreditSetting, Long> {
    Optional<BonusCreditSetting> findTopByOrderByIdAsc();
}
