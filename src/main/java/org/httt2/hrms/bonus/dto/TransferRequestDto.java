package org.httt2.hrms.bonus.dto;

import lombok.Data;
import org.httt2.hrms.bonus.entity.TransferType;

@Data
public class TransferRequestDto {
    private Long receiverId;
    private Integer points;
    private String note;
    private TransferType type;
}
