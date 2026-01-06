package org.httt2.hrms.bonus.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.httt2.hrms.bonus.entity.TransferType;

@Data
public class TransferRequestDto {
    @NotNull
    private Long receiverId;
    @NotNull
    @Positive
    private Integer points;
    private String note;
    private TransferType type;
}
