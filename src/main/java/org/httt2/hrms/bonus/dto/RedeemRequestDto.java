package org.httt2.hrms.bonus.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RedeemRequestDto {
    private Integer points;
    private BigDecimal amount;
}
