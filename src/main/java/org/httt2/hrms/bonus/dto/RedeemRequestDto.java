package org.httt2.hrms.bonus.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RedeemRequestDto {
    @NotNull(message = "Points is required")
    @Positive(message = "Points must be positive")
    private Integer points;

    private BigDecimal amount;

    private String userNote;
}
