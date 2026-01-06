package org.httt2.hrms.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.notification.dto.NotificationResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing SSE connections for real-time notifications.
 * Each employee can have one active SSE connection.
 */
@Service
@Slf4j
public class NotificationSseService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Creates a new SSE connection for an employee.
     *
     * @param empId the employee ID
     * @return SseEmitter for the connection
     */
    public SseEmitter createConnection(Long empId) {
        // Remove existing connection if any
        SseEmitter existing = emitters.remove(empId);
        if (existing != null) {
            try {
                existing.complete();
            } catch (Exception e) {
                log.warn("Error closing existing SSE connection for empId: {}", empId, e);
            }
        }

        // Create new connection with 10 minute timeout (reduced from 30 to prevent
        // resource leaks)
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);
        emitters.put(empId, emitter);

        // Handle completion and timeout
        emitter.onCompletion(() -> {
            log.info("SSE connection completed for empId: {}", empId);
            emitters.remove(empId);
        });

        emitter.onTimeout(() -> {
            log.info("SSE connection timeout for empId: {}", empId);
            emitters.remove(empId);
            try {
                emitter.complete();
            } catch (Exception e) {
                log.warn("Error completing SSE connection on timeout for empId: {}", empId, e);
            }
        });

        emitter.onError((ex) -> {
            log.error("SSE connection error for empId: {}", empId, ex);
            emitters.remove(empId);
            try {
                emitter.completeWithError(ex);
            } catch (Exception e) {
                log.warn("Error completing SSE connection on error for empId: {}", empId, e);
            }
        });

        // Send initial connection event
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE connection established"));
            log.info("SSE connection established for empId: {}", empId);
        } catch (IOException e) {
            log.error("Error sending initial SSE event for empId: {}", empId, e);
            emitters.remove(empId);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * Sends a notification to a specific employee via SSE.
     *
     * @param empId        the employee ID
     * @param notification the notification to send
     */
    public void sendNotification(Long empId, NotificationResponse notification) {
        SseEmitter emitter = emitters.get(empId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
                log.debug("Sent notification via SSE to empId: {}", empId);
            } catch (IOException e) {
                log.error("Error sending notification via SSE to empId: {}", empId, e);
                emitters.remove(empId);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.warn("Error completing SSE connection for empId: {}", empId, ex);
                }
            }
        } else {
            log.debug("No active SSE connection for empId: {}", empId);
        }
    }

    /**
     * Sends an unread count update to a specific employee via SSE.
     *
     * @param empId the employee ID
     * @param count the unread count
     */
    public void sendUnreadCount(Long empId, Long count) {
        SseEmitter emitter = emitters.get(empId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("unread-count")
                        .data(count));
                log.debug("Sent unread count via SSE to empId: {}", empId);
            } catch (IOException e) {
                log.error("Error sending unread count via SSE to empId: {}", empId, e);
                emitters.remove(empId);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.warn("Error completing SSE connection for empId: {}", empId, ex);
                }
            }
        }
    }

    /**
     * Closes the SSE connection for an employee.
     *
     * @param empId the employee ID
     */
    public void closeConnection(Long empId) {
        SseEmitter emitter = emitters.remove(empId);
        if (emitter != null) {
            try {
                emitter.complete();
                log.info("SSE connection closed for empId: {}", empId);
            } catch (Exception e) {
                log.warn("Error closing SSE connection for empId: {}", empId, e);
            }
        }
    }

    /**
     * Periodic cleanup task to remove stale SSE connections.
     * Runs every 5 minutes to clean up any connections that weren't properly
     * closed.
     * This helps prevent resource leaks if the frontend doesn't properly close
     * connections.
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupStaleConnections() {
        int initialSize = emitters.size();
        if (initialSize == 0) {
            return; // No connections to clean up
        }

        Iterator<Map.Entry<Long, SseEmitter>> iterator = emitters.entrySet().iterator();
        int removed = 0;

        while (iterator.hasNext()) {
            Map.Entry<Long, SseEmitter> entry = iterator.next();
            SseEmitter emitter = entry.getValue();

            // Check if emitter is already completed or has errors by trying to send a
            // heartbeat
            try {
                // Try to send a heartbeat to check if connection is still alive
                // If it throws an exception, the connection is dead
                emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
            } catch (Exception e) {
                // Connection is dead, remove it
                log.debug("Removing stale SSE connection for empId: {}", entry.getKey());
                iterator.remove();
                removed++;
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    // Ignore errors during cleanup - connection is already dead
                }
            }
        }

        if (removed > 0) {
            log.info("Cleaned up {} stale SSE connection(s). Active connections: {}", removed, emitters.size());
        }
    }

    /**
     * Gets the number of active SSE connections.
     * Useful for monitoring and debugging.
     *
     * @return the number of active connections
     */
    public int getActiveConnectionCount() {
        return emitters.size();
    }
}
