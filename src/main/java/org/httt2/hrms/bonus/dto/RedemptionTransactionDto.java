package org.httt2.hrms.bonus.dto;

import lombok.Data;
import org.httt2.hrms.bonus.entity.RedemptionTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RedemptionTransactionDto {

    private Long redemptionId;
    private Integer convertedPoint;
    private BigDecimal amountReceived;
    private LocalDateTime createdAt;

    public static RedemptionTransactionDto from(RedemptionTransaction entity) {
        RedemptionTransactionDto dto = new RedemptionTransactionDto();
        dto.setRedemptionId(entity.getRedemptionId());
        dto.setConvertedPoint(entity.getConvertedPoint());
        dto.setAmountReceived(entity.getAmountReceived());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
