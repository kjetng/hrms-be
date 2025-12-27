package org.httt2.hrms.bonus.service;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.bonus.dto.TransferRequestDto;
import org.httt2.hrms.bonus.entity.BonusPointAccount;
import org.httt2.hrms.bonus.entity.TransferTransaction;
import org.httt2.hrms.bonus.repository.BonusPointAccountRepository;
import org.httt2.hrms.bonus.repository.TransferTransactionRepository;
//import org.httt2.hrms.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BonusPointTransferService {

    private final BonusPointAccountRepository accountRepo;
    private final TransferTransactionRepository transferRepo;
//    private final SecurityUtil securityUtil;

    @Transactional
    public void transfer(TransferRequestDto request) {

//        Long senderId = securityUtil.getCurrentEmployeeId();
        // FOR DEVELOPING PURPOSE
        Long senderId = 2L;



        BonusPointAccount sender = accountRepo.findById(senderId)
                .orElseThrow();

        BonusPointAccount receiver = accountRepo.findById(request.getReceiverId())
                .orElseThrow();

        if (sender.getBonusPoint() < request.getPoints()) {
            throw new IllegalStateException("Insufficient points");
        }

        sender.setBonusPoint(sender.getBonusPoint() - request.getPoints());
        receiver.setBonusPoint(receiver.getBonusPoint() + request.getPoints());

        transferRepo.save(
                TransferTransaction.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .numberPoint(request.getPoints())
                        .note(request.getNote())
                        .build()
        );
    }
}
