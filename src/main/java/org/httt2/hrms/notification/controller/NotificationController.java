package org.httt2.hrms.notification.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.httt2.hrms.auth.config.JwtService;
import org.httt2.hrms.auth.repository.UserRepository;
import org.httt2.hrms.notification.dto.CreateNotificationRequest;
import org.httt2.hrms.notification.dto.NotificationListResponse;
import org.httt2.hrms.notification.dto.NotificationResponse;
import org.httt2.hrms.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.httt2.hrms.notification.service.NotificationSseService;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationSseService notificationSseService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    /**
     * Send a notification to an employee.
     * This endpoint publishes the notification to RabbitMQ, making it available for
     * both backends.
     */
    @PostMapping
    public ResponseEntity<Void> sendNotification(
            @RequestBody @Valid CreateNotificationRequest request) {
        notificationService.sendNotification(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Get all notifications for the current authenticated user with pagination.
     */
    @GetMapping
    public ResponseEntity<NotificationListResponse> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long empId = getCurrentUserEmpId();
        Page<NotificationResponse> notificationsPage = notificationService.getNotificationsByEmpId(empId, page, size);
        Long unreadCount = notificationService.getUnreadCount(empId);

        NotificationListResponse response = NotificationListResponse.builder()
                .notifications(notificationsPage.getContent())
                .totalUnread(unreadCount)
                .totalPages(notificationsPage.getTotalPages())
                .totalElements(notificationsPage.getTotalElements())
                .currentPage(page)
                .pageSize(size)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get unread notifications for the current authenticated user.
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {
        Long empId = getCurrentUserEmpId();
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(empId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get the count of unread notifications for the current authenticated user.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        Long empId = getCurrentUserEmpId();
        Long count = notificationService.getUnreadCount(empId);
        return ResponseEntity.ok(count);
    }

    /**
     * Mark a specific notification as read.
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        Long empId = getCurrentUserEmpId();
        notificationService.markAsRead(notificationId, empId);
        return ResponseEntity.ok().build();
    }

    /**
     * Mark all notifications as read for the current authenticated user.
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        Long empId = getCurrentUserEmpId();
        notificationService.markAllAsRead(empId);
        return ResponseEntity.ok().build();
    }

    /**
     * SSE endpoint for real-time notifications.
     * Establishes a Server-Sent Events connection to receive notifications in
     * real-time.
     * 
     * Event types:
     * - "connected": Initial connection confirmation
     * - "notification": New notification received
     * - "unread-count": Updated unread count
     */
    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter streamNotifications() {
        Long empId = getCurrentUserEmpId();
        return notificationSseService.createConnection(empId);
    }

    /**
     * Closes the SSE connection for the current authenticated user.
     * This should be called when the user logs out or when the frontend wants to
     * explicitly close the connection.
     */
    @DeleteMapping("/stream")
    public ResponseEntity<Void> closeStream() {
        Long empId = getCurrentUserEmpId();
        notificationSseService.closeConnection(empId);
        return ResponseEntity.ok().build();
    }

    /**
     * Helper method to get the current authenticated user's empId.
     * Extracts empId from JWT token to avoid unnecessary database queries,
     * especially important for long-lived SSE connections to prevent connection
     * leaks.
     */
    private Long getCurrentUserEmpId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("No authenticated user found");
        }

        // Try to extract empId from JWT token first (no database query)
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Long empId = jwtService.extractEmpIdFromRequest(request);
            if (empId != null) {
                return empId;
            }

            // For SSE endpoints, also check query parameter
            if (request.getRequestURI().contains("/notifications/stream")) {
                String token = request.getParameter("token");
                if (token != null) {
                    empId = jwtService.extractEmpIdFromToken(token);
                    if (empId != null) {
                        return empId;
                    }
                }
            }
        }

        // Fallback to database query if JWT extraction fails (shouldn't happen in
        // normal flow)
        var email = authentication.getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getEmpId() == null) {
            throw new RuntimeException("User does not have an associated employee ID");
        }

        return user.getEmpId();
    }
}
