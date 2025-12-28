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

    private final BonusPointAccountRepository accountRepo;
    private final RedemptionTransactionRepository redemptionRepo;
    private final TransferTransactionRepository transferRepo;
//    private final SecurityUtil securityUtil;
    private final BonusPointViewMapper mapper;

    public BonusPointViewDto getMyBonusPointView() {

//        Long empId = securityUtil.getCurrentEmployeeId();
        // FOR DEVELOPING PURPOSES
        Long empId = 1L;

        BonusPointAccount account = accountRepo.findById(empId)
                .orElseThrow(() -> new IllegalStateException("Account not found"));

        List<RedemptionTransaction> redemptions =
                redemptionRepo.findByAccount_EmpId(empId);

        List<TransferTransaction> sent =
                transferRepo.findBySender_EmpId(empId);

        List<TransferTransaction> received =
                transferRepo.findByReceiver_EmpId(empId);

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

        // TRANSFER - SENT
                for (TransferTransaction t : sent) {
                    history.add(
                            HistoryItemDto.builder()
                                    .id(t.getTransferId())
                                    .type(HistoryType.TRANSFER)
                                    .points(-t.getNumberPoint())
                                    .counterpartyId(t.getReceiver().getEmpId())
                                    .counterpartyName(t.getReceiver().getEmployee().getFullName())
                                    .note(t.getNote())
                                    .createdAt(t.getCreatedAt())
                                    .build()
                    );
                }

        // TRANSFER - RECEIVED
                for (TransferTransaction t : received) {
                    history.add(
                            HistoryItemDto.builder()
                                    .id(t.getTransferId())
                                    .type(HistoryType.TRANSFER)
                                    .points(t.getNumberPoint())
                                    .counterpartyId(t.getSender().getEmpId())
                                    .counterpartyName(t.getSender().getEmployee().getFullName())
                                    .note(t.getNote())
                                    .createdAt(t.getCreatedAt())
                                    .build()
                    );
                }

        // optional: AWARD / DEDUCT from admin table

        history.sort(
                java.util.Comparator
                        .comparing(HistoryItemDto::getCreatedAt)
                        .reversed()
        );

        return mapper.toViewDto(account, history);
    }
}
