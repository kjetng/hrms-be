package org.httt2.hrms.bonus.service;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.bonus.dto.BonusPointViewDto;
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
        // FOR DEVELOPING PURPOSE
        Long empId = 1L;

        BonusPointAccount account = accountRepo.findById(empId)
                .orElseThrow(() -> new IllegalStateException("Account not found"));

        List<RedemptionTransaction> redemptions =
                redemptionRepo.findByAccount_EmpId(empId);

        List<TransferTransaction> sent =
                transferRepo.findBySender_EmpId(empId);

        List<TransferTransaction> received =
                transferRepo.findByReceiver_EmpId(empId);

        return mapper.toViewDto(account, redemptions, sent, received);
    }
}
