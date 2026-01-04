package org.httt2.hrms.common.external.employee.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.common.external.employee.EmployeeRepository;
import org.httt2.hrms.common.external.employee.dto.EmployeeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EmployeeRepositoryImpl implements EmployeeRepository {

    private final RestTemplate restTemplate; // 👈 Cần Bean RestTemplate trong config

    // 👇 URL của Service .NET (Cấu hình trong application.properties)
    // Ví dụ: external.employee-service.url=http://localhost:5000/api/employees
    @Value("${external.employee-service.url:http://localhost:5188/api/Employees}") 
    private String employeeServiceUrl;

    @Override
    public EmployeeResponse getById(Long id) {
        try {
            // Gọi API: GET http://dotnet-host/api/employees/{id}
            String url = employeeServiceUrl + "/" + id;
            return restTemplate.getForObject(url, EmployeeResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch employee info from .NET service for ID: {}", id, e);
            return null;
        }
    }

    @Override
    public EmployeeResponse getOneByEmail(String email) {
        return null; // Chưa dùng tới
    }
}