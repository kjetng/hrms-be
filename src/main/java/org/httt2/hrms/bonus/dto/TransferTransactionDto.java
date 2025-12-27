package org.httt2.hrms.bonus.dto;

import lombok.Data;
import org.httt2.hrms.bonus.entity.TransferTransaction;

import java.time.LocalDateTime;

@Data
public class TransferTransactionDto {

    private Long transferId;
    private Integer numberPoint;
    private String note;
    private LocalDateTime createdAt;

    public static TransferTransactionDto from(TransferTransaction entity) {
        TransferTransactionDto dto = new TransferTransactionDto();
        dto.setTransferId(entity.getTransferId());
        dto.setNumberPoint(entity.getNumberPoint());
        dto.setNote(entity.getNote());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
