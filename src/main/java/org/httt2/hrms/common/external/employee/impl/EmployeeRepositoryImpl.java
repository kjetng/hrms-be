package org.httt2.hrms.common.external.employee.impl;

import org.httt2.hrms.common.external.employee.EmployeeRepository;
import org.httt2.hrms.common.external.employee.dto.EmployeeResponse;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeRepositoryImpl implements EmployeeRepository {
  @Override
  public EmployeeResponse getOneByEmail(String email) {
    return null;
  }
}
