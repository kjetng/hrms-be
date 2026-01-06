package org.httt2.hrms.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.notification.dto.CreateNotificationRequest;
import org.httt2.hrms.notification.dto.NotificationEvent;
import org.httt2.hrms.notification.dto.NotificationResponse;
import org.httt2.hrms.notification.entity.Notification;
import org.httt2.hrms.notification.repository.NotificationRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final RabbitTemplate rabbitTemplate;
    private final NotificationSseService notificationSseService;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing-key.notification}")
    private String notificationRoutingKey;

    /**
     * Sends a notification by publishing it to RabbitMQ.
     * This allows both Spring Boot and .NET backends to send notifications.
     */
    public void sendNotification(CreateNotificationRequest request) {
        log.info("Sending notification to empId: {}", request.getEmpId());

        NotificationEvent event = NotificationEvent.builder()
                .empId(request.getEmpId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .build();

        try {
            rabbitTemplate.convertAndSend(exchangeName, notificationRoutingKey, event);
            log.info("Notification event published to RabbitMQ for empId: {}", request.getEmpId());
        } catch (Exception e) {
            log.error("Failed to publish notification event to RabbitMQ for empId: {}", request.getEmpId(), e);
            throw new RuntimeException("Failed to send notification", e);
        }
    }

    /**
     * Creates a notification directly in the database.
     * This is called by the RabbitMQ consumer when a notification event is received.
     */
    @Transactional
    public Notification createNotification(NotificationEvent event) {
        log.info("Creating notification for empId: {}", event.getEmpId());

        Notification notification = Notification.builder()
                .empId(event.getEmpId())
                .title(event.getTitle())
                .message(event.getMessage())
                .type(event.getType())
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created with ID: {} for empId: {}", saved.getNotificationId(), event.getEmpId());
        return saved;
    }

    /**
     * Gets all notifications for an employee with pagination.
     */
    public Page<NotificationResponse> getNotificationsByEmpId(Long empId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByEmpIdOrderByCreatedAtDesc(empId, pageable);

        return notifications.map(this::mapToResponse);
    }

    /**
     * Gets unread notifications for an employee.
     */
    public List<NotificationResponse> getUnreadNotifications(Long empId) {
        List<Notification> notifications = notificationRepository.findByEmpIdAndIsReadFalseOrderByCreatedAtDesc(empId);
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Marks a notification as read.
     */
    @Transactional
    public void markAsRead(Long notificationId, Long empId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getEmpId().equals(empId)) {
            throw new RuntimeException("Notification does not belong to this employee");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
        log.info("Notification {} marked as read for empId: {}", notificationId, empId);
        
        // Send updated unread count via SSE
        Long unreadCount = getUnreadCount(empId);
        notificationSseService.sendUnreadCount(empId, unreadCount);
    }

    /**
     * Marks all notifications as read for an employee.
     */
    @Transactional
    public void markAllAsRead(Long empId) {
        List<Notification> unreadNotifications = notificationRepository.findByEmpIdAndIsReadFalseOrderByCreatedAtDesc(empId);
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
        log.info("All notifications marked as read for empId: {}", empId);
        
        // Send updated unread count via SSE (should be 0)
        notificationSseService.sendUnreadCount(empId, 0L);
    }

    /**
     * Gets the count of unread notifications for an employee.
     */
    public Long getUnreadCount(Long empId) {
        return notificationRepository.countByEmpIdAndIsReadFalse(empId);
    }

    public NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .empId(notification.getEmpId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

