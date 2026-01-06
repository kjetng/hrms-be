package org.httt2.hrms.notification.repository;

import org.httt2.hrms.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByEmpIdOrderByCreatedAtDesc(Long empId, Pageable pageable);

    List<Notification> findByEmpIdAndIsReadFalseOrderByCreatedAtDesc(Long empId);

    Long countByEmpIdAndIsReadFalse(Long empId);
}

