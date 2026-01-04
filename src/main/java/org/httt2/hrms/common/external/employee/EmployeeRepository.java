package org.httt2.hrms.common.external.employee;

import org.httt2.hrms.common.external.employee.dto.EmployeeResponse;

public interface EmployeeRepository {
  EmployeeResponse getById(Long id);
  EmployeeResponse getOneByEmail(String email);
}
