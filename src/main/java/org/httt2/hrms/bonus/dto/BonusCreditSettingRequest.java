package org.httt2.hrms.bonus.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonusCreditSettingRequest {

    @NotNull
    @Positive
    @Min(1)
    private Integer baseBonusCredits;

    @NotNull
    @Positive
    private Double conversionRate;

    @NotNull
    private LocalDate date;
}
