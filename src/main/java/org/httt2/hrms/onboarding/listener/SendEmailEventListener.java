package org.httt2.hrms.onboarding.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.onboarding.dto.SendEmailEvent;
import org.httt2.hrms.onboarding.service.EmailService;
import org.httt2.hrms.onboarding.service.OnboardingTokenService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer that listens for sendEmail events.
 * When a new employee profile is created, this listener receives the event,
 * generates a secure onboarding form link, and sends an email to the employee.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SendEmailEventListener {

    private final OnboardingTokenService onboardingTokenService;
    private final EmailService emailService;

    /**
     * Handles the sendEmail event from RabbitMQ.
     *
     * @param event the send email event containing employee information
     */
    @RabbitListener(queues = "${rabbitmq.queue.send-email}")
    public void handleSendEmailEvent(SendEmailEvent event) {
        log.info("Received sendEmail event for employee: {} (ID: {})",
                event.getFullName(), event.getEmployeeId());

        try {
            // Validate the event
            if (event.getEmployeeId() == null || event.getPersonalEmail() == null) {
                log.error("Invalid event received: employeeId or personalEmail is null");
                return;
            }

            // Generate secure form link
            String secureFormUrl = onboardingTokenService.generateSecureFormLink(
                    event.getEmployeeId(),
                    event.getPersonalEmail()
            );
            log.info("Generated secure form URL for employee: {}", event.getEmployeeId());

            // Send onboarding email
            emailService.sendOnboardingEmail(
                    event.getPersonalEmail(),
                    event.getFullName(),
                    secureFormUrl
            );

            log.info("Successfully processed sendEmail event for employee: {}", event.getEmployeeId());

        } catch (Exception e) {
            log.error("Failed to process sendEmail event for employee: {}", event.getEmployeeId(), e);
            // Depending on your requirements, you might want to:
            // - Retry the message
            // - Send to a dead letter queue
            // - Trigger an alert
            throw e; // Re-throw to trigger RabbitMQ retry mechanism
        }
    }
}

