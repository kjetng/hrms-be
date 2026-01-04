package org.httt2.hrms.common.external.employee.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ManagerEmployeeResponse(
        Long id,
        String fullName,
        String email,
        Long positionId,
        Long departmentId,
        String status) {
}
