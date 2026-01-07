package org.httt2.hrms.common.external.employee.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmployeeResponse(
    long id,
    String personalEmail, // Tự động map nếu JSON key là "personalEmail"
    String fullName,
    @JsonProperty("departmentId") 
    Long departmentId
) {
}