package org.httt2.hrms.bankaccount.repository;

import org.httt2.hrms.bankaccount.dto.BankAccountRecordDto;

import java.util.List;

public interface BankAccountRepository {
    List<BankAccountRecordDto> getMyBankAccounts();
}
