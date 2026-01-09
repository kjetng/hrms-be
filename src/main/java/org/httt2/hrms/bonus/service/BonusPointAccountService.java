package org.httt2.hrms.bonus.service;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.bonus.dto.BonusPointAccountDto;
import org.httt2.hrms.bonus.dto.CreateBonusPointAccountRequest;
import org.httt2.hrms.bonus.entity.BonusPointAccount;
import org.httt2.hrms.bonus.repository.BonusPointAccountRepository;
import org.httt2.hrms.common.external.employee.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BonusPointAccountService {

    private final BonusPointAccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public BonusPointAccountDto createAccount(CreateBonusPointAccountRequest request) {
        var emp = employeeRepository.getOneById(request.getEmpId());
        if (emp == null) {
            throw new IllegalArgumentException("Employee not found: " + request.getEmpId());
        }
        if (accountRepository.existsById(request.getEmpId())) {
            throw new IllegalStateException("Bonus point account already exists for empId: " + request.getEmpId());
        }

        int initialPoints = request.getBonusPoint() == null ? 0 : request.getBonusPoint();
        BonusPointAccount saved = accountRepository.save(
                BonusPointAccount.builder()
                        .empId(request.getEmpId())
                        .bonusPoint(initialPoints)
                        .build());

        BonusPointAccountDto dto = new BonusPointAccountDto();
        dto.setEmpId(saved.getEmpId());
        dto.setBonusPoint(saved.getBonusPoint());
        return dto;
    }
}
