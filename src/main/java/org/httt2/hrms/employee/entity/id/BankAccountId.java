package org.httt2.hrms.employee.entity.id;

import lombok.Data;

import java.io.Serializable;

@Data
public class BankAccountId implements Serializable {
  private String accountNumber;
  private String bankName;
}
