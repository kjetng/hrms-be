package org.httt2.hrms.bankaccount.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.bankaccount.dto.BankAccountRecordDto;
import org.httt2.hrms.bankaccount.repository.BankAccountRepository;
import org.httt2.hrms.bankaccount.service.BankAccountService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    @Override
    public BankAccountRecordDto getMyLatestBankAccount() {
        List<BankAccountRecordDto> accounts = bankAccountRepository.getMyBankAccounts();
        return accounts.isEmpty() ? null : accounts.get(accounts.size() - 1);
    }
}
