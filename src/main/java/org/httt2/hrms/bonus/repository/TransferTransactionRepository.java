package org.httt2.hrms.bonus.repository;

import org.httt2.hrms.bonus.entity.TransferTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferTransactionRepository
        extends JpaRepository<TransferTransaction, Long>, TransferTransactionRepositoryCustom {

    List<TransferTransaction> findBySender_EmpId(Long empId);
    List<TransferTransaction> findByReceiver_EmpId(Long empId);
    List<TransferTransaction> findBySender_EmpIdOrReceiver_EmpId(
            Long senderEmpId,
            Long receiverEmpId
    );
}
