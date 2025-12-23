package org.httt2.hrms.bonus.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonusCreditSettingDto {

    @NotNull
    @Min(1)
    @Max(999_999_999)
    private Integer baseBonusCredits;

    @NotNull
    @Min(1000)
    @Max(999_999_999)
    private Integer conversionRate;

    @NotNull
    @Min(1)
    @Max(28)
    private Integer date;
}
