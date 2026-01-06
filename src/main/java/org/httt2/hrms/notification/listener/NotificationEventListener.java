package org.httt2.hrms.notification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.notification.dto.NotificationEvent;
import org.httt2.hrms.notification.service.NotificationService;
import org.httt2.hrms.notification.service.NotificationSseService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer that listens for notification events.
 * This consumer stores notifications in the database when received from RabbitMQ.
 * Both Spring Boot and .NET backends can publish to this queue.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final NotificationSseService notificationSseService;

    /**
     * Handles the notification event from RabbitMQ.
     *
     * @param event the notification event containing empId, title, message, and type
     */
    @RabbitListener(queues = "${rabbitmq.queue.notification}")
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Received notification event for empId: {}", event.getEmpId());

        try {
            if (event.getEmpId() == null || event.getTitle() == null || event.getMessage() == null) {
                log.error("Invalid notification event: empId, title, or message is null");
                return;
            }

            var notification = notificationService.createNotification(event);
            log.info("Successfully created notification for empId: {}", event.getEmpId());

            // Push notification to SSE if employee is connected
            var notificationResponse = notificationService.mapToResponse(notification);
            notificationSseService.sendNotification(event.getEmpId(), notificationResponse);
            
            // Also send updated unread count
            Long unreadCount = notificationService.getUnreadCount(event.getEmpId());
            notificationSseService.sendUnreadCount(event.getEmpId(), unreadCount);

        } catch (Exception e) {
            log.error("Failed to create notification for empId: {}", event.getEmpId(), e);
            throw e;
        }
    }
}

