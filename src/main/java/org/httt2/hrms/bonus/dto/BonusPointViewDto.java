package org.httt2.hrms.bonus.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BonusPointViewDto {

    private Long empId;
    private Integer currentBalance;
    private String role;

    private Integer totalRedeemed;
    private Integer totalSent;
    private Integer totalReceived;
    private long totalRecords;

    private LocalDate dateFrom;
    private LocalDate dateTo;

    private Integer page;
    private Integer size;

    private List<HistoryItemDto> history;

}
