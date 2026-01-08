package org.httt2.hrms.bonus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.auth.entity.User;
import org.httt2.hrms.auth.repository.UserRepository;
import org.httt2.hrms.bonus.entity.BonusCreditSetting;
import org.httt2.hrms.bonus.entity.BonusPointAccount;
import org.httt2.hrms.bonus.entity.TransferTransaction;
import org.httt2.hrms.bonus.entity.TransferType;
import org.httt2.hrms.bonus.repository.BonusCreditSettingRepository;
import org.httt2.hrms.bonus.repository.BonusPointAccountRepository;
import org.httt2.hrms.bonus.repository.TransferTransactionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyCreditDistributionService {

    private final BonusCreditSettingRepository creditSettingRepository;
    private final UserRepository userRepository;
    private final BonusPointAccountRepository accountRepository;
    private final TransferTransactionRepository transferRepository;

    /**
     * Runs daily just after midnight and distributes monthly credits
     * when today matches the configured `creditDate`.
     */
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void runMonthlyDistribution() {
        distributeIfDue(false);
    }

    /**
     * Manual trigger entry. If `force` is true, bypass day-of-month check
     * but still respect idempotence for the current day.
     */
    @Transactional
    public void runMonthlyDistributionNow(boolean force) {
        distributeIfDue(force);
    }

    private void distributeIfDue(boolean force) {
        BonusCreditSetting setting = creditSettingRepository.findTopByOrderByIdAsc().orElse(null);
        if (setting == null) {
            log.warn("Monthly distribution skipped: no BonusCreditSetting found.");
            return;
        }

        LocalDate today = LocalDate.now();
        int todayDay = today.getDayOfMonth();
        Integer creditDay = setting.getCreditDate();
        if (creditDay == null || creditDay <= 0) {
            log.warn("Monthly distribution skipped: invalid creditDate={}", creditDay);
            return;
        }

        if (!force && todayDay != creditDay) {
            // Not the configured distribution day
            return;
        }

        // Monthly idempotence guard
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);
        boolean alreadyRanThisMonth = transferRepository.existsByTypeAndCreatedAtBetween(
                TransferType.MONTHLY, startOfMonth, endOfMonth);

        if (alreadyRanThisMonth && !force) {
            log.info("Monthly distribution already executed for {}, skipping.", currentMonth);
            return;
        }

        int baseCredits = setting.getBaseBonusCredits() != null ? setting.getBaseBonusCredits() : 0;
        int pointsToDistribute = baseCredits;
        if (pointsToDistribute <= 0) {
            log.warn("Monthly distribution skipped: non-positive points (base={}).", baseCredits);
            return;
        }

        List<User> users = userRepository.findAll();
        int processed = 0;

        // Get or create system sender account with empId = -1
        BonusPointAccount systemSender = accountRepository.findById(-1L)
                .orElseGet(() -> {
                    BonusPointAccount sys = BonusPointAccount.builder()
                            .empId(-1L)
                            .bonusPoint(0)
                            .build();
                    return accountRepository.save(sys);
                });

        String noteMonth = currentMonth.toString(); // e.g., "2026-01"
        String noteSuffix = alreadyRanThisMonth ? "*" : "";
        String noteText = "Monthly credit distribution for " + noteMonth + noteSuffix;

        for (User user : users) {
            Long empId = user.getEmpId();
            if (empId == null)
                continue;

            BonusPointAccount account = accountRepository.findById(empId)
                    .orElseGet(() -> BonusPointAccount.builder()
                            .empId(empId)
                            .bonusPoint(0)
                            .build());

            account.setBonusPoint((account.getBonusPoint() != null ? account.getBonusPoint() : 0) + pointsToDistribute);
            accountRepository.save(account);

            TransferTransaction tx = TransferTransaction.builder()
                    .sender(systemSender)
                    .receiver(account)
                    .numberPoint(pointsToDistribute)
                    .note(noteText)
                    .type(TransferType.MONTHLY)
                    .createdAt(LocalDateTime.now())
                    .build();
            transferRepository.save(tx);
            processed++;
        }

        log.info("Monthly distribution executed for {} users on {} ({} points each).", processed, today,
                pointsToDistribute);
    }
}
