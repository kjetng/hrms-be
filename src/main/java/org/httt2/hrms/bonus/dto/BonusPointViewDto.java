package org.httt2.hrms.bonus.dto;

import lombok.Data;

import java.util.List;

@Data
public class BonusPointViewDto {

    private Long empId;
    private Integer currentBalance;

    private Integer totalRedeemed;
    private Integer totalSent;
    private Integer totalReceived;

    private List<RedemptionTransactionDto> redemptions;
    private List<TransferTransactionDto> sentTransfers;
    private List<TransferTransactionDto> receivedTransfers;
}
