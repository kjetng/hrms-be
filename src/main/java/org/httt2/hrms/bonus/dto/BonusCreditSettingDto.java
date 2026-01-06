package org.httt2.hrms.bonus.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.httt2.hrms.bonus.validation.annotation.MultipleOf;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonusCreditSettingDto {

    @NotNull(message = "Base bonus credits is required")
    @Min(value = 1, message = "Base bonus credits must be at least 1")
    @Max(value = 999_999_999, message = "Base bonus credits cannot exceed 999,999,999")
    private Integer baseBonusCredits;

    @NotNull(message = "Conversion rate is required")
    @Min(value = 1000, message = "Conversion rate must be at least 1,000")
    @Max(value = 999_999_999, message = "Conversion rate cannot exceed 999,999,999")
    @MultipleOf(value = 1000, message = "Conversion rate must be a multiple of 1,000")
    private Integer conversionRate;

    @NotNull(message = "Date is required")
    @Min(value = 1, message = "Date must be between 1 and 28")
    @Max(value = 28, message = "Date must be between 1 and 28")
    private Integer date;
}
