package org.httt2.hrms.bonus.service;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.auth.config.JwtService;
import org.httt2.hrms.bonus.dto.*;
import org.httt2.hrms.bonus.entity.BonusPointAccount;
import org.httt2.hrms.bonus.entity.RedemptionTransaction;
import org.httt2.hrms.bonus.entity.TransferTransaction;
import org.httt2.hrms.bonus.entity.TransferType;
import org.httt2.hrms.bonus.mapper.BonusPointViewMapper;
import org.httt2.hrms.bonus.repository.BonusPointAccountRepository;
import org.httt2.hrms.bonus.repository.RedemptionTransactionRepository;
import org.httt2.hrms.bonus.repository.TransferTransactionRepository;
import org.httt2.hrms.common.external.employee.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;

    @Transactional(readOnly = false)
    public BonusPointBalanceDto getMyBalance() {
        Long empId = extractEmpIdFromRequest();
        if (empId == null) {
            throw new IllegalStateException("User not authenticated or empId not found in token");
        }

        BonusPointAccount account = accountRepo.findById(empId)
                .orElseGet(() -> accountRepo.save(
                        BonusPointAccount.builder()
                                .empId(empId)
                                .bonusPoint(0)
                                .build()));

        BonusPointBalanceDto dto = new BonusPointBalanceDto();
        dto.setBonusPoint(account.getBonusPoint());
        dto.setRole(extractRoleFromRequest());
        return dto;
    }

    @Transactional(readOnly = false)
    public BonusPointViewDto getMyBonusPointView(
            BonusPointViewQueryDto query) {
        Long empId = extractEmpIdFromRequest();
        if (empId == null) {
            throw new IllegalStateException("User not authenticated or empId not found in token");
        }

        BonusPointAccount account = accountRepo.findById(empId)
                .orElseGet(() -> accountRepo.save(
                        BonusPointAccount.builder()
                                .empId(empId)
                                .bonusPoint(0)
                                .build()));

        List<HistoryItemDto> history = new ArrayList<>();

        Map<Long, String> employeeNameCache = new HashMap<>();

        // ===== REDEEM =====
        for (RedemptionTransaction r : redemptionRepo.findByAccount_EmpId(empId)) {

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
                            .build());
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
                required);

        // fetch redemptions limited only if REDEEM is requested (or no types filter)
        boolean includeRedeem = query.getTypes() == null || query.getTypes().isEmpty()
                || query.getTypes().contains(HistoryType.REDEEM);
        List<RedemptionTransaction> redemptions = new ArrayList<>();
        long redemptionCount = 0;
        if (includeRedeem) {
            java.time.LocalDateTime fromDt = from == null ? null : from.atStartOfDay();
            java.time.LocalDateTime toDt = to == null ? null : java.time.LocalDateTime.of(to, java.time.LocalTime.MAX);
            if (fromDt == null)
                fromDt = java.time.LocalDateTime.of(1970, 1, 1, 0, 0);
            if (toDt == null)
                toDt = java.time.LocalDateTime.of(3000, 1, 1, 0, 0);

            redemptionCount = redemptionRepo.countByAccount_EmpIdAndCreatedAtBetween(empId, fromDt, toDt);
            var pageReq = org.springframework.data.domain.PageRequest.of(0, required,
                    org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                            "createdAt"));
            redemptions = redemptionRepo.findByAccount_EmpIdAndCreatedAtBetween(empId, fromDt, toDt, pageReq)
                    .getContent();
        }

        // map + merge + sort
        List<HistoryItemDto> transferHistory = new ArrayList<>();
        for (TransferTransaction t : transfers)
            transferHistory.add(mapTransferToHistory(t, empId, employeeNameCache));

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
                            .build());
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
        dto.setRole(extractRoleFromRequest());
        return dto;
    }

    // ================= HELPERS =================

    private HistoryItemDto mapTransferToHistory(
            TransferTransaction t,
            Long empId,
            Map<Long, String> employeeNameCache) {
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
        // AWARD means points are given FROM system TO user
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
        // DEDUCT means points are taken FROM user TO system
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
                    .counterpartyName(resolveEmployeeName(receiverId, employeeNameCache))
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
                .counterpartyName(resolveEmployeeName(senderId, employeeNameCache))
                .note(t.getNote())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private String resolveEmployeeName(Long id, Map<Long, String> cache) {
        if (id == null)
            return null;
        if (cache.containsKey(id))
            return cache.get(id);

        if (SYSTEM_EMP_ID.equals(id)) {
            cache.put(id, "System");
            return "System";
        }

        var employee = employeeRepository.getOneById(id);
        String fullName = employee == null ? null : employee.fullName();
        cache.put(id, fullName);
        return fullName;
    }

    private List<HistoryItemDto> applyQuery(
            List<HistoryItemDto> history,
            BonusPointViewQueryDto query) {
        return history.stream()
                .filter(item -> matchesDate(item, query.getDateRange()))
                .filter(item -> matchesType(item, query.getTypes()))
                .sorted(buildComparator(query.getSort()))
                .toList();
    }

    private boolean matchesDate(
            HistoryItemDto item,
            BonusPointViewQueryDto.DateRange range) {
        if (range == null)
            return true;

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
            List<HistoryType> types) {
        return types == null || types.isEmpty()
                || types.contains(item.getType());
    }

    private Comparator<HistoryItemDto> buildComparator(
            BonusPointViewQueryDto.Sort sort) {
        Comparator<HistoryItemDto> comparator = Comparator.comparing(HistoryItemDto::getCreatedAt);

        if (sort == null || sort.getDirection() == SortDirection.DESC) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private Long extractEmpIdFromRequest() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            Long empId = jwtService.extractEmpIdFromRequest(request);
            if (empId != null) {
                return empId;
            }
        }

        // Fallback: try to get from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails userDetails) {
            if (userDetails instanceof org.httt2.hrms.auth.entity.User user) {
                return user.getEmpId();
            }
        }

        return null;
    }

    private String extractRoleFromRequest() {
        // Try to get from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails userDetails) {
            if (userDetails instanceof org.httt2.hrms.auth.entity.User user) {
                return user.getRole() != null ? user.getRole().name() : null;
            }
        }
        return null;
    }
}
