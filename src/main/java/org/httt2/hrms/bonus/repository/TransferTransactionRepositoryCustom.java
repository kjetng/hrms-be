package org.httt2.hrms.bonus.repository;

import org.httt2.hrms.bonus.dto.HistoryType;
import org.httt2.hrms.bonus.dto.SortDirection;
import org.httt2.hrms.bonus.dto.BonusPointViewQueryDto;
import org.httt2.hrms.bonus.entity.TransferTransaction;

import java.time.LocalDate;
import java.util.List;

public interface TransferTransactionRepositoryCustom {
    List<TransferTransaction> findFilteredForEmployee(
            Long empId,
            LocalDate from,
            LocalDate to,
            List<HistoryType> types,
            String sortField,
            SortDirection direction
    );

    // limited fetch (useful for pagination merging)
    List<TransferTransaction> findFilteredForEmployee(
            Long empId,
            LocalDate from,
            LocalDate to,
            List<HistoryType> types,
            String sortField,
            SortDirection direction,
            Integer limit
    );

    // count matching transfers (for pagination total)
    long countFilteredForEmployee(
            Long empId,
            LocalDate from,
            LocalDate to,
            List<HistoryType> types
    );
}