package org.httt2.hrms.bonus.repository;

import org.httt2.hrms.bonus.entity.RedemptionTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RedemptionTransactionRepository
        extends JpaRepository<RedemptionTransaction, Long> {

    List<RedemptionTransaction> findByAccount_EmpId(Long empId);
}
