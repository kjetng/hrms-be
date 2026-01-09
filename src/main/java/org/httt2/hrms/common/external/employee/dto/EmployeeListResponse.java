package org.httt2.hrms.common.external.employee.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeListResponse {
    private List<EmployeeResponse> data;

    public List<EmployeeResponse> getData() {
        return data;
    }

    public void setData(List<EmployeeResponse> data) {
        this.data = data;
    }
}