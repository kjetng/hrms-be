package org.httt2.hrms.bonus.dto;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Builder
public class HistoryItemDto {

    private Long id;

    private HistoryType type;     // REDEMPTION, TRANSFER_SENT, TRANSFER_RECEIVED

    private Integer points;       // signed (+ / -)

    private BigDecimal amount;    // only for redemption
    private String note;          // optional

    private Long counterpartyId;
    private String counterpartyName;

    private String currency;
    private LocalDateTime createdAt;
}
