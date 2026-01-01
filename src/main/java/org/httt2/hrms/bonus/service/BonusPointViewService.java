package org.httt2.hrms.bonus.service;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.bonus.dto.BonusPointViewDto;
import org.httt2.hrms.bonus.dto.HistoryType;
import org.httt2.hrms.bonus.dto.HistoryItemDto;
import org.httt2.hrms.bonus.entity.BonusPointAccount;
import org.httt2.hrms.bonus.entity.RedemptionTransaction;
import org.httt2.hrms.bonus.entity.TransferTransaction;
import org.httt2.hrms.bonus.mapper.BonusPointViewMapper;
import org.httt2.hrms.bonus.repository.BonusPointAccountRepository;
import org.httt2.hrms.bonus.repository.RedemptionTransactionRepository;
import org.httt2.hrms.bonus.repository.TransferTransactionRepository;
//import org.httt2.hrms.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BonusPointViewService {
    // CHECK THIS OUT!!! TODO
    private static final Long TEST_EMP_ID = 1L;

    private final BonusPointAccountRepository accountRepo;
    private final RedemptionTransactionRepository redemptionRepo;
    private final TransferTransactionRepository transferRepo;
//    private final SecurityUtil securityUtil;
    private final BonusPointViewMapper mapper;
    private static final Long SYSTEM_EMP_ID = -1L;

    // TYPE-ASSIGNER
    private HistoryItemDto mapTransferToHistory(
            TransferTransaction t,
            Long currentEmpId
    ) {
        Long senderId = t.getSender().getEmpId();
        Long receiverId = t.getReceiver().getEmpId();

        // 🎁 AWARD (system → employee)
        if (senderId.equals(SYSTEM_EMP_ID)) {
            return HistoryItemDto.builder()
                    .id(t.getTransferId())
                    .type(HistoryType.AWARD)
                    .points(t.getNumberPoint()) // positive
                    .note(t.getNote())
                    .createdAt(t.getCreatedAt())
                    .build();
        }

        // ⚠️ DEDUCT (employee → system)
        if (receiverId.equals(SYSTEM_EMP_ID)) {
            return HistoryItemDto.builder()
                    .id(t.getTransferId())
                    .type(HistoryType.DEDUCT)
                    .points(-t.getNumberPoint()) // negative
                    .note(t.getNote())
                    .createdAt(t.getCreatedAt())
                    .build();
        }

        // 🔁 TRANSFER - SENT
        if (senderId.equals(currentEmpId)) {
            return HistoryItemDto.builder()
                    .id(t.getTransferId())
                    .type(HistoryType.TRANSFER_SENT)
                    .points(-t.getNumberPoint())
                    .counterpartyId(receiverId)
                    .counterpartyName(
                            t.getReceiver().getEmployee().getFullName()
                    )
                    .note(t.getNote())
                    .createdAt(t.getCreatedAt())
                    .build();
        }

        // 🔁 TRANSFER - RECEIVED
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


    public BonusPointViewDto getMyBonusPointView() {

//        Long empId = securityUtil.getCurrentEmployeeId();
        // FOR DEVELOPING PURPOSES TODO
        Long empId = TEST_EMP_ID;


        BonusPointAccount account = accountRepo.findById(empId)
                .orElseThrow(() -> new IllegalStateException("Account not found"));

        List<RedemptionTransaction> redemptions =
                redemptionRepo.findByAccount_EmpId(empId);

        List<TransferTransaction> transfers =
                transferRepo.findBySender_EmpIdOrReceiver_EmpId(empId, empId);

        List<HistoryItemDto> history = new ArrayList<>();

        // REDEEM
        for (RedemptionTransaction r : redemptions) {
            history.add(
                    HistoryItemDto.builder()
                            .id(r.getRedemptionId())
                            .type(HistoryType.REDEEM)
                            .points(-r.getConvertedPoint())
                            .amount(r.getAmountReceived())
                            .currency("USD")
                            .createdAt(r.getCreatedAt())
                            .build()
            );
        }

        // TRANSFER
        for (TransferTransaction t : transfers) {
            history.add(mapTransferToHistory(t, empId));
        }


        history.sort(
                java.util.Comparator
                        .comparing(HistoryItemDto::getCreatedAt)
                        .reversed()
        );

        return mapper.toViewDto(account, history);
    }
}
