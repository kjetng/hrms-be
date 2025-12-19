package org.httt2.hrms.onboarding.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the message received from RabbitMQ when a new employee profile is created.
 * This message triggers the onboarding email flow.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendEmailEvent {

    /**
     * The unique identifier of the employee
     */
    private Long employeeId;

    /**
     * The personal email address of the employee where the onboarding form link will be sent
     */
    private String personalEmail;

    /**
     * The full name of the employee
     */
    private String fullName;

    /**
     * The work email address that will be assigned to the employee (optional)
     */
    private String workEmail;
}

