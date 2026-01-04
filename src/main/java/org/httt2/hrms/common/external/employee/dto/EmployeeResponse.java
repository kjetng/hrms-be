package org.httt2.hrms.common.external.employee.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmployeeResponse(
    long id,
    String personalEmail // Tự động map nếu JSON key là "personalEmail"
) {
}