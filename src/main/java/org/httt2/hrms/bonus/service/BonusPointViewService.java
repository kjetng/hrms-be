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

            // filter by date
            if (query.getDateRange() != null) {
                LocalDate from = query.getDateRange().getFrom();
                LocalDate to = query.getDateRange().getTo();
                LocalDate date = r.getCreatedAt().toLocalDate();
                if ((from != null && date.isBefore(from)) || (to != null && date.isAfter(to))) {
                    continue;
                }
            }

            // filter by types
            if (query.getTypes() != null && !query.getTypes().isEmpty()
                    && !query.getTypes().contains(HistoryType.REDEEM)) {
                continue;
            }

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

        // ===== TRANSFERS / AWARD / DEDUCT / MONTHLY (paged) =====
        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int size = query.getSize() == null || query.getSize() < 1 ? 10 : query.getSize();
        int required = page * size; // fetch enough items from each source to build the merged page

        LocalDate from = null;
        LocalDate to = null;
        if (query.getDateRange() != null) {
            from = query.getDateRange().getFrom();
            to = query.getDateRange().getTo();
        }

        // fetch transfers limited
        List<TransferTransaction> transfers = transferRepo.findFilteredForEmployee(
                empId,
                from,
                to,
                query.getTypes(),
                query.getSort() == null ? null : query.getSort().getField(),
                query.getSort() == null ? null : query.getSort().getDirection(),
                required
        );

        // fetch redemptions limited only if REDEEM is requested (or no types filter)
        boolean includeRedeem = query.getTypes() == null || query.getTypes().isEmpty() || query.getTypes().contains(HistoryType.REDEEM);
        List<RedemptionTransaction> redemptions = new ArrayList<>();
        long redemptionCount = 0;
        if (includeRedeem) {
            java.time.LocalDateTime fromDt = from == null ? null : from.atStartOfDay();
            java.time.LocalDateTime toDt = to == null ? null : java.time.LocalDateTime.of(to, java.time.LocalTime.MAX);
            if (fromDt == null) fromDt = java.time.LocalDateTime.MIN;
            if (toDt == null) toDt = java.time.LocalDateTime.MAX;

            redemptionCount = redemptionRepo.countByAccount_EmpIdAndCreatedAtBetween(empId, fromDt, toDt);
            var pageReq = org.springframework.data.domain.PageRequest.of(0, required, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
            redemptions = redemptionRepo.findByAccount_EmpIdAndCreatedAtBetween(empId, fromDt, toDt, pageReq).getContent();
        }

        // map + merge + sort
        List<HistoryItemDto> transferHistory = new ArrayList<>();
        for (TransferTransaction t : transfers) transferHistory.add(mapTransferToHistory(t, empId));

        List<HistoryItemDto> redemptionHistory = new ArrayList<>();
        for (RedemptionTransaction r : redemptions) {
            redemptionHistory.add(
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

        List<HistoryItemDto> merged = new ArrayList<>();
        merged.addAll(transferHistory);
        merged.addAll(redemptionHistory);
        merged.sort(buildComparator(query.getSort()));

        // slice for requested page
        long transferCount = transferRepo.countFilteredForEmployee(empId, from, to, query.getTypes());
        long totalRecords = transferCount + redemptionCount;
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, merged.size());
        List<HistoryItemDto> pagedHistory = fromIndex >= merged.size() ? List.of() : merged.subList(fromIndex, toIndex);

        BonusPointViewDto dto = mapper.toViewDto(account, pagedHistory);

        dto.setTotalRecords(totalRecords);
        if (query.getDateRange() != null) {
            dto.setDateFrom(query.getDateRange().getFrom());
            dto.setDateTo(query.getDateRange().getTo());
        }
        dto.setPage(page);
        dto.setSize(size);
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
