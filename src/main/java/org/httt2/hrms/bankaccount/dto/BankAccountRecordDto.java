package org.httt2.hrms.bankaccount.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankAccountRecordDto {
    private String accountNumber;
    private String bankName;
    private String accountName;
    private Long employeeId;
}
