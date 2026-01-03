package org.httt2.hrms.bonus.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BonusPointViewQueryDto {

    private DateRange dateRange;
    private List<HistoryType> types;
    private Sort sort;
    private Integer page;  // 1-based
    private Integer size;
    @Data
    public static class DateRange {
        private LocalDate from;
        private LocalDate to;
    }

    @Data
    public static class Sort {
        private String field;          // currently only "createdAt"
        private SortDirection direction;
    }
}
