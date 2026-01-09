package org.httt2.hrms.bonus.repository;

import org.httt2.hrms.bonus.entity.RedemptionTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    
    /**
     * Sum total redeemed points for an employee.
     */
    @Query("SELECT COALESCE(SUM(r.convertedPoint), 0) FROM RedemptionTransaction r WHERE r.account.empId = :empId")
    Integer sumConvertedPointByEmpId(@Param("empId") Long empId);
}
