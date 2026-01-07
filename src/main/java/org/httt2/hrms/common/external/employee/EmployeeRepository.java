package org.httt2.hrms.common.external.employee;

import org.httt2.hrms.common.external.employee.dto.EmployeeResponse;
import org.httt2.hrms.common.external.employee.dto.ManagerEmployeeResponse;

import java.util.List;

public interface EmployeeRepository {

  EmployeeResponse getOneById(Long id);

  List<ManagerEmployeeResponse> getDirectReports(Long managerId);
}
