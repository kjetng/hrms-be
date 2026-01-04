package org.httt2.hrms.common.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer that listens for sendEmail events.
 * This listener only consumes messages and sends emails.
 * All email content is decided by the producer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SendEmailEventListener {

    private final EmailService emailService;

    /**
     * Handles the sendEmail event from RabbitMQ.
     *
     * @param event the send email event containing email address and content
     */
    @RabbitListener(queues = "${rabbitmq.queue.send-email}")
    public void handleSendEmailEvent(SendEmailEvent event) {
        log.info("Received sendEmail event for: {}", event.getEmailToSend());

        try {
            if (event.getEmailToSend() == null || event.getSubject() == null || event.getHtmlContent() == null) {
                log.error("Invalid event: emailToSend, subject, or htmlContent is null");
                return;
            }

            emailService.sendEmail(
                    event.getEmailToSend(),
                    event.getSubject(),
                    event.getHtmlContent());

            log.info("Successfully sent email to: {}", event.getEmailToSend());

        } catch (Exception e) {
            log.error("Failed to send email to: {}", event.getEmailToSend(), e);
            throw e;
        }
    }
}
