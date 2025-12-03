package org.httt2.hrms.auth.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@RequiredArgsConstructor
public enum Role {

  // 1. Standard Employee (Can see their own data, apply for leave)
  USER,

  // 2. Manager (Can approve requests, see their department)
  MANAGER,

  // 3. Human Resources (Can add new employees, manage campaigns)
  HR,

  // 4. IT Admin (Can configure the system, manage user accounts)
  ADMIN;

  /**
   * Helper method to convert the enum to a Spring Security Authority.
   * Spring Security usually expects roles to start with "ROLE_" for hasRole() checks.
   */
  public List<SimpleGrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + this.name()));
  }
}
