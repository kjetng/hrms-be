package org.httt2.hrms.common.email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the message received from RabbitMQ for sending an email.
 * The producer is responsible for defining all email content.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendEmailEvent {

    /**
     * The email address to send to
     */
    private String emailToSend;

    /**
     * The email subject
     */
    private String subject;

    /**
     * The email body content (HTML)
     */
    private String htmlContent;
}

