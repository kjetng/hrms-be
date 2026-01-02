package org.httt2.hrms.bonus.service;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.bonus.dto.*;
import org.httt2.hrms.bonus.entity.BonusPointAccount;
import org.httt2.hrms.bonus.entity.RedemptionTransaction;
import org.httt2.hrms.bonus.entity.TransferTransaction;
import org.httt2.hrms.bonus.entity.TransferType;
import org.httt2.hrms.bonus.mapper.BonusPointViewMapper;
import org.httt2.hrms.bonus.repository.BonusPointAccountRepository;
import org.httt2.hrms.bonus.repository.RedemptionTransactionRepository;
import org.httt2.hrms.bonus.repository.TransferTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BonusPointViewService {

    private static final Long TEST_EMP_ID = 1L;
    private static final Long SYSTEM_EMP_ID = -1L;

    private final BonusPointAccountRepository accountRepo;
    private final RedemptionTransactionRepository redemptionRepo;
    private final TransferTransactionRepository transferRepo;
    private final BonusPointViewMapper mapper;

    public BonusPointViewDto getMyBonusPointView(
            BonusPointViewQueryDto query
    ) {
        Long empId = TEST_EMP_ID;

        BonusPointAccount account = accountRepo.findById(empId)
                .orElseThrow(() -> new IllegalStateException("Account not found"));

        List<HistoryItemDto> history = new ArrayList<>();

        // ===== REDEEM =====
        for (RedemptionTransaction r :
                redemptionRepo.findByAccount_EmpId(empId)) {

            history.add(
                    HistoryItemDto.builder()
                            .id(r.getRedemptionId())
                            .type(HistoryType.REDEEM)
                            .points(r.getConvertedPoint())
                            .amount(r.getAmountReceived())
                            .currency("USD")
                            .createdAt(r.getCreatedAt())
                            .build()
            );
        }

        // ===== TRANSFERS / AWARD / DEDUCT ===== 
        List<TransferTransaction> transfers =
                transferRepo.findBySender_EmpIdOrReceiver_EmpId(empId, empId);

        for (TransferTransaction t : transfers) {
            history.add(mapTransferToHistory(t, empId));
        }


        // ===== FILTER + SORT =====
        history = applyQuery(history, query);

        // ===== RECORDS COUNT + RETURN DTO ===== 
        BonusPointViewDto dto =
                mapper.toViewDto(account, history);

        dto.setTotalRecords(history.size());
        if (query.getDateRange() != null) {
            dto.setDateFrom(query.getDateRange().getFrom());
            dto.setDateTo(query.getDateRange().getTo());
        }
        return dto;
    }

    // ================= HELPERS =================

    private HistoryItemDto mapTransferToHistory(
            TransferTransaction t,
            Long empId
    ) {
        Long senderId = t.getSender().getEmpId();
        Long receiverId = t.getReceiver().getEmpId();

        // 🗓️ MONTHLY
        if (t.getType() != null && t.getType() == TransferType.MONTHLY) {
            return HistoryItemDto.builder()
                    .id(t.getTransferId())
                    .type(HistoryType.MONTHLY)
                    .points(t.getNumberPoint())
                    .note(t.getNote())
                    .createdAt(t.getCreatedAt())
                    .build();
        }

        // 🎁 AWARD — prefer explicit DB type, fall back to sender==SYSTEM
        if (t.getType() == TransferType.AWARD || (t.getType() == null && senderId.equals(SYSTEM_EMP_ID))) {
            return HistoryItemDto.builder()
                    .id(t.getTransferId())
                    .type(HistoryType.AWARD)
                    .points(t.getNumberPoint())
                    .note(t.getNote())
                    .createdAt(t.getCreatedAt())
                    .build();
        }

        // ⚠️ DEDUCT — prefer explicit DB type, fall back to receiver==SYSTEM
        if (t.getType() == TransferType.DEDUCT || (t.getType() == null && receiverId.equals(SYSTEM_EMP_ID))) {
            return HistoryItemDto.builder()
                    .id(t.getTransferId())
                    .type(HistoryType.DEDUCT)
                    .points(t.getNumberPoint())
                    .note(t.getNote())
                    .createdAt(t.getCreatedAt())
                    .build();
        }

        // 🔁 TRANSFER SENT
        if (senderId.equals(empId)) {
            return HistoryItemDto.builder()
                    .id(t.getTransferId())
                    .type(HistoryType.TRANSFER_SENT)
                    .points(t.getNumberPoint())
                    .counterpartyId(receiverId)
                    .counterpartyName(
                            t.getReceiver().getEmployee().getFullName()
                    )
                    .note(t.getNote())
                    .createdAt(t.getCreatedAt())
                    .build();
        }

        // 🔁 TRANSFER RECEIVED
        return HistoryItemDto.builder()
                .id(t.getTransferId())
                .type(HistoryType.TRANSFER_RECEIVED)
                .points(t.getNumberPoint())
                .counterpartyId(senderId)
                .counterpartyName(
                        t.getSender().getEmployee().getFullName()
                )
                .note(t.getNote())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private List<HistoryItemDto> applyQuery(
            List<HistoryItemDto> history,
            BonusPointViewQueryDto query
    ) {
        return history.stream()
                .filter(item -> matchesDate(item, query.getDateRange()))
                .filter(item -> matchesType(item, query.getTypes()))
                .sorted(buildComparator(query.getSort()))
                .toList();
    }

    private boolean matchesDate(
            HistoryItemDto item,
            BonusPointViewQueryDto.DateRange range
    ) {
        if (range == null) return true;

        LocalDate date = item.getCreatedAt().toLocalDate();

        if (range.getFrom() != null && date.isBefore(range.getFrom())) {
            return false;
        }
        if (range.getTo() != null && date.isAfter(range.getTo())) {
            return false;
        }
        return true;
    }

    private boolean matchesType(
            HistoryItemDto item,
            List<HistoryType> types
    ) {
        return types == null || types.isEmpty()
                || types.contains(item.getType());
    }

    private Comparator<HistoryItemDto> buildComparator(
            BonusPointViewQueryDto.Sort sort
    ) {
        Comparator<HistoryItemDto> comparator =
                Comparator.comparing(HistoryItemDto::getCreatedAt);

        if (sort == null || sort.getDirection() == SortDirection.DESC) {
            comparator = comparator.reversed();
        }
        return comparator;
    }
}
