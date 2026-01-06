package org.httt2.hrms.common.external.employee.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmployeeResponse(
        Long id,
        String fullName,
        String firstName,
        String lastName,
        String email,
        Long managerId,
        String status,
        String departmentName,
        String positionTitle) {
}
