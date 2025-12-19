package org.httt2.hrms.onboarding.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for sending onboarding emails to employees.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends an onboarding email to the employee with a secure form link.
     *
     * @param toEmail       the recipient's email address
     * @param employeeName  the name of the employee
     * @param secureFormUrl the secure URL for the onboarding form
     */
    @Async
    public void sendOnboardingEmail(String toEmail, String employeeName, String secureFormUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to HRMS - Complete Your Onboarding");

            String htmlContent = buildOnboardingEmailContent(employeeName, secureFormUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Onboarding email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send onboarding email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send onboarding email", e);
        }
    }

    private String buildOnboardingEmailContent(String employeeName, String secureFormUrl) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Welcome to HRMS</title>
                </head>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                        <h1 style="color: white; margin: 0;">Welcome to HRMS!</h1>
                    </div>
                    
                    <div style="background-color: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
                        <h2 style="color: #333;">Hello %s,</h2>
                        
                        <p>Welcome to our team! We're excited to have you on board.</p>
                        
                        <p>To complete your onboarding process, please click the button below to access your secure onboarding form. This form will help us gather the necessary information to set up your employee profile.</p>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" 
                               style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                                      color: white; 
                                      padding: 15px 30px; 
                                      text-decoration: none; 
                                      border-radius: 5px; 
                                      font-weight: bold;
                                      display: inline-block;">
                                Complete Your Onboarding
                            </a>
                        </div>
                        
                        <p style="color: #666; font-size: 14px;">
                            <strong>Important:</strong> This link is valid for 7 days and can only be used once. 
                            If you have any issues accessing the form, please contact your HR department.
                        </p>
                        
                        <p style="color: #666; font-size: 14px;">
                            If the button above doesn't work, copy and paste this URL into your browser:<br>
                            <a href="%s" style="color: #667eea; word-break: break-all;">%s</a>
                        </p>
                        
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        
                        <p style="color: #999; font-size: 12px; text-align: center;">
                            This is an automated message from HRMS. Please do not reply to this email.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(employeeName, secureFormUrl, secureFormUrl, secureFormUrl);
    }
}

