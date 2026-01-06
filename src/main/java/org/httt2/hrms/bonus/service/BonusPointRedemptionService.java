package org.httt2.hrms.bonus.service;


import lombok.RequiredArgsConstructor;
import org.httt2.hrms.bonus.dto.BonusPointViewDto;
import org.httt2.hrms.bonus.entity.BonusPointAccount;
import org.httt2.hrms.bonus.entity.RedemptionTransaction;
import org.httt2.hrms.bonus.entity.TransferTransaction;
import org.httt2.hrms.bonus.mapper.BonusPointViewMapper;
import org.httt2.hrms.bonus.repository.BonusPointAccountRepository;
import org.httt2.hrms.bonus.repository.RedemptionTransactionRepository;
import org.httt2.hrms.bonus.repository.TransferTransactionRepository;
//import org.httt2.hrms.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BonusPointRedemptionService {
}
