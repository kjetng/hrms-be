package org.httt2.hrms.bonus.mapper;

import org.httt2.hrms.bonus.dto.BonusPointViewDto;
import org.httt2.hrms.bonus.dto.RedemptionTransactionDto;
import org.httt2.hrms.bonus.dto.TransferTransactionDto;
import org.httt2.hrms.bonus.entity.BonusPointAccount;
import org.httt2.hrms.bonus.entity.RedemptionTransaction;
import org.httt2.hrms.bonus.entity.TransferTransaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BonusPointViewMapper {

    public BonusPointViewDto toViewDto(
            BonusPointAccount account,
            List<RedemptionTransaction> redemptions,
            List<TransferTransaction> sent,
            List<TransferTransaction> received) {

        BonusPointViewDto dto = new BonusPointViewDto();
        dto.setEmpId(account.getEmpId());
        dto.setCurrentBalance(account.getBonusPoint());

        dto.setTotalRedeemed(
                redemptions.stream().mapToInt(RedemptionTransaction::getConvertedPoint).sum()
        );
        dto.setTotalSent(
                sent.stream().mapToInt(TransferTransaction::getNumberPoint).sum()
        );
        dto.setTotalReceived(
                received.stream().mapToInt(TransferTransaction::getNumberPoint).sum()
        );

        dto.setRedemptions(
                redemptions.stream().map(RedemptionTransactionDto::from).toList()
        );
        dto.setSentTransfers(
                sent.stream().map(TransferTransactionDto::from).toList()
        );
        dto.setReceivedTransfers(
                received.stream().map(TransferTransactionDto::from).toList()
        );

        return dto;
    }
}
