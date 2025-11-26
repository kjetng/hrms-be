package org.httt2.hrms.employee.entity;

import jakarta.persistence.*;
import lombok.*;
import org.httt2.hrms.employee.entity.id.BankAccountId;

@Entity
@Table(name = "bank_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Using a composite key as implied by the diagram,
// or you could use a generated ID for simplicity. Here is the composite ID approach:
@IdClass(BankAccountId.class)
public class BankAccount {

  @Id
  private String accountNumber;

  @Id
  private String bankName;

  private String accountName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "emp_id", nullable = false)
  @ToString.Exclude
  private Employee employee;
}