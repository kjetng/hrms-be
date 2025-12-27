package org.httt2.hrms.bonus.dto;

import lombok.Data;

@Data
public class TransferRequestDto {
    private Long receiverId;
    private Integer points;
    private String note;
}
