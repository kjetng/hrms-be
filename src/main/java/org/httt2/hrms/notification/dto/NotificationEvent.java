package org.httt2.hrms.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic notification event DTO for RabbitMQ.
 * This format can be used by both Spring Boot and .NET backend services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationEvent {

    /**
     * The employee ID to send the notification to
     */
    private Long empId;

    /**
     * The notification title
     */
    private String title;

    /**
     * The notification message/content
     */
    private String message;

    /**
     * Optional notification type (e.g., "info", "warning", "success", "error")
     */
    private String type;
}

