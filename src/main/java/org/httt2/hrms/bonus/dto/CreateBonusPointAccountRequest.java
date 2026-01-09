package org.httt2.hrms.bonus.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBonusPointAccountRequest {

    @NotNull
    private Long empId;

    @Min(0)
    private Integer bonusPoint;
}
