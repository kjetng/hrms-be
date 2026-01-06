package org.httt2.hrms.bankaccount.controller;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.bankaccount.dto.BankAccountRecordDto;
import org.httt2.hrms.bankaccount.service.BankAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bankaccount")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @GetMapping("/me")
    public ResponseEntity<BankAccountRecordDto> getMyBankAccount() {
        BankAccountRecordDto bankAccount = bankAccountService.getMyLatestBankAccount();
        if (bankAccount == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bankAccount);
    }
}
