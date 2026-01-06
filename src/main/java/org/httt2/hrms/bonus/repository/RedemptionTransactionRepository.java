package org.httt2.hrms.bonus.repository;

import org.httt2.hrms.bonus.entity.RedemptionTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RedemptionTransactionRepository
        extends JpaRepository<RedemptionTransaction, Long> {

    List<RedemptionTransaction> findByAccount_EmpId(Long empId);

    Page<RedemptionTransaction> findByAccount_EmpIdAndCreatedAtBetween(
            Long empId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    long countByAccount_EmpIdAndCreatedAtBetween(
            Long empId,
            LocalDateTime from,
            LocalDateTime to
    );
}
