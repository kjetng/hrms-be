package org.httt2.hrms.bonus.repository;

import org.httt2.hrms.bonus.entity.BonusPointAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BonusPointAccountRepository
        extends JpaRepository<BonusPointAccount, Long> {
}
